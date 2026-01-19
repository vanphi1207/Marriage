package me.ihqqq.marriage.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.model.MarriageRecord;
import me.ihqqq.marriage.model.MarriageSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public class SqliteStorage implements Storage {

    private final MarriagePlugin plugin;
    private HikariDataSource dataSource;

    public SqliteStorage(MarriagePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Void> init() {
        return CompletableFuture.runAsync(() -> {
            try {
                File dataFolder = plugin.getDataFolder();
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }
                
                HikariConfig config = new HikariConfig();
                String dbFile = plugin.getConfig().getString("database.sqlite.file", "marriage.db");
                File file = new File(dataFolder, dbFile);
                config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
                config.setPoolName("Marriage-SQLite-Pool");
                config.setMaximumPoolSize(1);
                
                config.addDataSourceProperty("cachePrepStmts", "false");
                this.dataSource = new HikariDataSource(config);

                
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS marriage (" +
                                    "uuid_a TEXT NOT NULL, " +
                                    "uuid_b TEXT NOT NULL, " +
                                    "since INTEGER NOT NULL, " +
                                    "home_location TEXT, " +
                                    "allow_teleport INTEGER NOT NULL, " +
                                    "chat_enabled INTEGER NOT NULL, " +
                                    "settings_json TEXT, " +
                                    "PRIMARY KEY (uuid_a, uuid_b))");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveMarriage(MarriageRecord record) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO marriage" +
                    " (uuid_a, uuid_b, since, home_location, allow_teleport, chat_enabled, settings_json) " +
                    " VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, record.getUuidA().toString());
                ps.setString(2, record.getUuidB().toString());
                ps.setLong(3, record.getSince());
                ps.setString(4, locationToString(record.getHome()));
                ps.setInt(5, record.getSettings().isAllowTeleport() ? 1 : 0);
                ps.setInt(6, record.getSettings().isChatEnabled() ? 1 : 0);
                ps.setString(7, flagsToJson(record.getSettings().getFlags()));
                ps.executeUpdate();
            } catch (Exception e) {
                plugin.getLogger().severe("Error saving marriage record: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteMarriage(UUID player) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM marriage WHERE uuid_a = ? OR uuid_b = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, player.toString());
                ps.setString(2, player.toString());
                ps.executeUpdate();
            } catch (Exception e) {
                plugin.getLogger().severe("Error deleting marriage record: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<MarriageRecord> findByPlayer(UUID player) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM marriage WHERE uuid_a = ? OR uuid_b = ? LIMIT 1";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, player.toString());
                ps.setString(2, player.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        UUID uuidA = UUID.fromString(rs.getString("uuid_a"));
                        UUID uuidB = UUID.fromString(rs.getString("uuid_b"));
                        long since = rs.getLong("since");
                        String homeStr = rs.getString("home_location");
                        boolean allowTeleport = rs.getInt("allow_teleport") == 1;
                        boolean chatEnabled = rs.getInt("chat_enabled") == 1;
                        String settingsJson = rs.getString("settings_json");
                        Location home = stringToLocation(homeStr);
                        Map<String, Object> flags = jsonToFlags(settingsJson);
                        MarriageSettings settings = new MarriageSettings(allowTeleport, chatEnabled, flags);
                        return new MarriageRecord(uuidA, uuidB, since, home, settings);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error finding marriage record: " + e.getMessage());
            }
            return null;
        });
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private String locationToString(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    private Location stringToLocation(String str) {
        if (str == null || str.isEmpty()) return null;
        String[] parts = str.split(",");
        if (parts.length < 6) return null;
        try {
            String worldName = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        } catch (Exception e) {
            return null;
        }
    }

    private String flagsToJson(Map<String, Object> flags) {
        if (flags == null || flags.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : flags.entrySet()) {
            if (!first) sb.append(",");
            sb.append('"').append(entry.getKey()).append('"').append(":");
            Object value = entry.getValue();
            if (value instanceof Number || value instanceof Boolean) {
                sb.append(value);
            } else {
                sb.append('"').append(value.toString()).append('"');
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private Map<String, Object> jsonToFlags(String json) {
        Map<String, Object> map = new HashMap<>();
        if (json == null || json.isEmpty()) return map;
        try {
            String trimmed = json.trim();
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
                String[] entries = trimmed.split(",");
                for (String entry : entries) {
                    String[] kv = entry.split(":", 2);
                    if (kv.length != 2) continue;
                    String key = kv[0].trim().replaceAll("^\"|\"$", "");
                    String value = kv[1].trim();
                    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                        map.put(key, Boolean.parseBoolean(value));
                    } else {
                        try {
                            Double number = Double.parseDouble(value);
                            map.put(key, number);
                        } catch (NumberFormatException e) {
                            map.put(key, value.replaceAll("^\"|\"$", ""));
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse settings JSON: " + json);
        }
        return map;
    }
}