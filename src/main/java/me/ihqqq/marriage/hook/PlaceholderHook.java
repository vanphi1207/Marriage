package me.ihqqq.marriage.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.model.MarriageRecord;
import me.ihqqq.marriage.service.MarriageService;
import me.ihqqq.marriage.util.TimeUtil;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHook extends PlaceholderExpansion {

    private final MarriagePlugin plugin;
    private final MarriageService marriageService;

    public PlaceholderHook(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
        this.marriageService = plugin.getMarriageService();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "marriage";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) {
            return "";
        }
        params = params.toLowerCase();
        MarriageRecord record = marriageService.getMarriage(offlinePlayer.getUniqueId()).join();
        String notMarried = plugin.getConfig().getString("placeholders.notmarry", "Single");
        String married = plugin.getConfig().getString("placeholders.marry", "Married");
        switch (params) {
            case "notmarry":
                return notMarried;
            case "marry":
                return married;
            case "partner":
                return resolvePartnerName(offlinePlayer, record, notMarried);
            case "partner_formatted":
                return formatPartner(offlinePlayer, record, notMarried);
            case "since":
                if (record == null) return notMarried;
                return TimeUtil.formatDate(record.getSince());
            case "wedding_date":
                if (record == null) return notMarried;
                return TimeUtil.formatDate(record.getSince());
            case "anniversary":
                if (record == null) return notMarried;
                java.time.LocalDate date = java.time.Instant.ofEpochMilli(record.getSince())
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                return String.format("%02d/%02d", date.getDayOfMonth(), date.getMonthValue());
            case "status":
                return record == null ? notMarried : married;
            case "days":
                if (record == null) return "0";
                return String.valueOf(TimeUtil.daysBetween(record.getSince()));
            case "years":
                if (record == null) return "0";
                long years = java.time.temporal.ChronoUnit.YEARS.between(
                        java.time.Instant.ofEpochMilli(record.getSince()).atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                        java.time.LocalDate.now()
                );
                return String.valueOf(Math.max(years, 0));
            default:
                return null;
        }
    }

    private String resolvePartnerName(@NotNull OfflinePlayer player, @Nullable MarriageRecord record, @NotNull String fallback) {
        if (record == null) {
            return fallback;
        }
        String partner = plugin.getServer().getOfflinePlayer(record.getPartner(player.getUniqueId())).getName();
        return partner == null || partner.isBlank() ? fallback : partner;
    }

    private String formatPartner(@NotNull OfflinePlayer player, @Nullable MarriageRecord record, @NotNull String fallback) {
        String partner = resolvePartnerName(player, record, fallback);
        if (partner.equals(fallback)) {
            return fallback;
        }
        return "❤ " + partner;
    }
}
