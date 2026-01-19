package me.ihqqq.marriage;

import me.ihqqq.marriage.command.MarryAdminCommand;
import me.ihqqq.marriage.command.MarryCommand;
import me.ihqqq.marriage.hook.PlaceholderHook;
import me.ihqqq.marriage.message.MessageService;
import me.ihqqq.marriage.model.MarriageSettings;
import me.ihqqq.marriage.service.GiftService;
import me.ihqqq.marriage.service.MarriageService;
import me.ihqqq.marriage.service.RequestService;
import me.ihqqq.marriage.service.TeleportService;
import me.ihqqq.marriage.storage.MysqlStorage;
import me.ihqqq.marriage.storage.SqliteStorage;
import me.ihqqq.marriage.storage.Storage;
import me.ihqqq.marriage.util.SchedulerUtil;
import me.ihqqq.marriage.gui.menu.MenuManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;

public class MarriagePlugin extends JavaPlugin {

    private Storage storage;
    private MarriageService marriageService;
    private RequestService requestService;
    private TeleportService teleportService;
    private GiftService giftService;
    private MessageService messageService;
    private MarriageSettings marriageSettings;
    private SchedulerUtil schedulerUtil;
    private PlaceholderHook placeholderHook;
    private MenuManager menuManager;
    private FileConfiguration guiConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("gui.yml", false);
        saveResource("messages_en.yml", false);
        saveResource("messages_vi.yml", false);

        this.messageService = new MessageService(this);

        FileConfiguration config = getConfig();
        boolean allowTeleport = config.getBoolean("marriage.allowTeleport", true);
        boolean chatEnabled = config.getBoolean("marriage.chatEnabled", true);
        this.marriageSettings = new MarriageSettings(allowTeleport, chatEnabled, new HashMap<>());

        this.schedulerUtil = new SchedulerUtil(this);
        reloadGuiConfig();

        String type = config.getString("database.type", "sqlite").toLowerCase();
        if (type.equals("mysql")) {
            this.storage = new MysqlStorage(this);
        } else {
            this.storage = new SqliteStorage(this);
        }
        storage.init().whenComplete((v, ex) -> {
            if (ex != null) {
                getLogger().severe("Failed to initialise marriage storage: " + ex.getMessage());
                return;
            }
            getLogger().info("Marriage storage initialised successfully.");
        });

        this.marriageService = new MarriageService(this, storage, messageService);
        this.requestService = new RequestService(this, marriageService, messageService);
        this.teleportService = new TeleportService(this, marriageService, messageService);
        this.giftService = new GiftService(this);

        this.menuManager = new MenuManager(this);

        MarryCommand marryCommand = new MarryCommand(this);
        getCommand("marry").setExecutor(marryCommand);
        getCommand("marry").setTabCompleter(marryCommand);

        MarryAdminCommand adminCommand = new MarryAdminCommand(this);
        getCommand("marryadmin").setExecutor(adminCommand);
        getCommand("marryadmin").setTabCompleter(adminCommand);

        Bukkit.getPluginManager().registerEvents(menuManager, this);
        Bukkit.getPluginManager().registerEvents(requestService, this);
        Bukkit.getPluginManager().registerEvents(marriageService, this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.placeholderHook = new PlaceholderHook(this);
            this.placeholderHook.register();
        }
        sendLifecycleMessage("enabled", "<green>ENABLED</green>");
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.close();
        }
        if (placeholderHook != null) {
            placeholderHook.unregister();
        }
        sendLifecycleMessage("disabled", "<red>DISABLED</red>");
    }

    private void sendLifecycleMessage(@NotNull String state, @NotNull String stateLabel) {
        String version = getDescription().getVersion();
        int innerWidth = 34;
        String message = String.join("\n",
                "<gray>┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓</gray>",
                formatBoxLine("<gray>Marriage status:</gray> " + stateLabel, innerWidth),
                formatBoxLine("<gray>author:</gray> <white>ihqqq</white>", innerWidth),
                formatBoxLine("<gray>version:</gray> <white>" + version + "</white>", innerWidth),
                "<gray>┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛</gray>"
        );
        Component component = MiniMessage.miniMessage().deserialize(message);
        Bukkit.getConsoleSender().sendMessage(component);
        getLogger().info("Marriage plugin " + state + ".");
    }

    private String formatBoxLine(@NotNull String content, int width) {
        String plainContent = content.replaceAll("<[^>]*>", "");
        int padding = Math.max(0, width - plainContent.length());
        return "<gray>┃</gray> " + content + " ".repeat(padding) + "<gray>┃</gray>";
    }

    public Storage getStorage() {
        return storage;
    }

    public MarriageService getMarriageService() {
        return marriageService;
    }

    public RequestService getRequestService() {
        return requestService;
    }

    public TeleportService getTeleportService() {
        return teleportService;
    }

    public GiftService getGiftService() {
        return giftService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public SchedulerUtil getSchedulerUtil() {
        return schedulerUtil;
    }

    public MarriageSettings getMarriageSettings() {
        return marriageSettings;
    }

    public void setMarriageSettings(@NotNull MarriageSettings settings) {
        this.marriageSettings = settings;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }

    public void reloadGuiConfig() {
        File file = new File(getDataFolder(), "gui.yml");
        if (!file.exists()) {
            FileConfiguration generated = new YamlConfiguration();
            FileConfiguration config = getConfig();
            if (config.isConfigurationSection("gui")) {
                generated.set("gui", config.getConfigurationSection("gui"));
            }
            try {
                generated.save(file);
            } catch (java.io.IOException ignored) {
                this.guiConfig = generated;
                return;
            }
        }
        this.guiConfig = YamlConfiguration.loadConfiguration(file);
    }
}
