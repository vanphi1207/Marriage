package me.ihqqq.marriage.message;

import me.clip.placeholderapi.PlaceholderAPI;
import me.ihqqq.marriage.MarriagePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles loading and rendering of messages defined in messages.yml.  Messages
 * are formatted using MiniMessage and support PlaceholderAPI expansions as
 * well as custom tag placeholders such as <player>, <partner>, <since>
 * defined at runtime.  A prefix can also be injected via the <prefix> tag.
 */
public class MessageService {

    private final MarriagePlugin plugin;
    private final MiniMessage miniMessage;
    private Map<MessageKey, String> messages;
    private FileConfiguration config;
    private String prefix;

    public MessageService(@NotNull MarriagePlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        reload();
    }

    /**
     * Reloads all messages from the messages.yml file.  This method is
     * invoked when the plugin starts and whenever the reload command is run.
     */
    public void reload() {
        String language = plugin.getConfig().getString("messages.language", "en");
        String languageFile = "messages_" + language + ".yml";
        File file = new File(plugin.getDataFolder(), languageFile);
        if (!file.exists()) {
            try {
                plugin.saveResource(languageFile, false);
            } catch (IllegalArgumentException ignored) {
                // Language resource missing; fall back to messages.yml
            }
        }
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
            file = new File(plugin.getDataFolder(), "messages.yml");
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        this.messages = new HashMap<>();
        for (MessageKey key : MessageKey.values()) {
            String raw = this.config.getString(key.getPath());
            if (raw != null) {
                messages.put(key, raw);
            }
        }
        this.prefix = this.config.getString(MessageKey.PREFIX.getPath(), "");
    }

    /**
     * Sends a message to the specified receiver without any additional
     * placeholders.  If the message key is missing the path itself is used
     * instead.
     *
     * @param receiver the command sender
     * @param key      the message key
     */
    public void sendMessage(@NotNull CommandSender receiver, @NotNull MessageKey key) {
        sendMessage(receiver, key, Map.of());
    }

    /**
     * Sends a message with placeholders to the given receiver.  Placeholder
     * values must be plain strings or objects with sensible toString()
     * implementations.  Supported placeholders include <player>, <partner>,
     * <since>, <days>, <text>, <state>, <player2> and more.  A <prefix>
     * placeholder is automatically provided based on messages.yml.
     *
     * @param receiver     the command sender
     * @param key          the message key
     * @param placeholders placeholder map
     */
    public void sendMessage(@NotNull CommandSender receiver, @NotNull MessageKey key, @NotNull Map<String, Object> placeholders) {
        String template = messages.getOrDefault(key, key.getPath());
        sendTemplate(receiver, template, placeholders);
    }

    public void sendRawMessage(@NotNull CommandSender receiver, @NotNull String template, @NotNull Map<String, Object> placeholders) {
        sendTemplate(receiver, template, placeholders);
    }

    private void sendTemplate(@NotNull CommandSender receiver, @NotNull String template, @NotNull Map<String, Object> placeholders) {
        // Apply PlaceholderAPI expansions if available
        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") && receiver instanceof Player player) {
            template = PlaceholderAPI.setPlaceholders(player, template);
        }
        MessageTarget target = MessageTarget.CHAT;
        String trimmed = template.trim();
        String actionBarPrefix = "actionbar:";
        if (trimmed.regionMatches(true, 0, actionBarPrefix, 0, actionBarPrefix.length())) {
            target = MessageTarget.ACTION_BAR;
            template = trimmed.substring(actionBarPrefix.length()).trim();
        }
        // Build tag resolver for custom placeholders
        TagResolver.Builder builder = TagResolver.builder();
        // Prefix
        builder.resolver(TagResolver.resolver("prefix", (args, ctx) -> Tag.inserting(miniMessage.deserialize(prefix))));
        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            builder.resolver(TagResolver.resolver(name, (args, ctx) -> {
                // If the value contains MiniMessage formatting (<>) parse it
                String str = value == null ? "" : value.toString();
                return Tag.inserting(miniMessage.deserialize(str));
            }));
        }
        TagResolver resolver = builder.build();
        Component message = miniMessage.deserialize(template, resolver);
        // Send component
        if (target == MessageTarget.ACTION_BAR && receiver instanceof Player player) {
            player.sendActionBar(message);
        } else {
            receiver.sendMessage(message);
        }
    }


    @Nullable
    public String getRaw(@NotNull MessageKey key) {
        return messages.get(key);
    }

    @Nullable
    public String getString(@NotNull String path) {
        return config == null ? null : config.getString(path);
    }

    @NotNull
    public java.util.List<String> getStringList(@NotNull String path) {
        return config == null ? java.util.List.of() : config.getStringList(path);
    }


    public String getPrefix() {
        return prefix;
    }

    private enum MessageTarget {
        CHAT,
        ACTION_BAR
    }
}
