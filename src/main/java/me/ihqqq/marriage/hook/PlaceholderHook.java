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
        switch (params) {
            case "notmarry":
                return plugin.getConfig().getString("placeholders.notmarry", "Single");
            case "marry":
                return plugin.getConfig().getString("placeholders.marry", "Married");
            case "partner":
                if (record == null) return "";
                String partner = plugin.getServer().getOfflinePlayer(record.getPartner(offlinePlayer.getUniqueId())).getName();
                return partner == null ? "" : partner;
            case "since":
                if (record == null) return "";
                return TimeUtil.formatDate(record.getSince());
            case "status":
                return record == null
                        ? plugin.getConfig().getString("placeholders.notmarry", "Single")
                        : plugin.getConfig().getString("placeholders.marry", "Married");
            case "days":
                if (record == null) return "0";
                return String.valueOf(TimeUtil.daysBetween(record.getSince()));
            default:
                return null;
        }
    }
}
