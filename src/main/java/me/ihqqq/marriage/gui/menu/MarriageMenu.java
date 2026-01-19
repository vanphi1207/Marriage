package me.ihqqq.marriage.gui.menu;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.gui.component.MenuButton;
import me.ihqqq.marriage.message.MessageService;
import me.ihqqq.marriage.service.TeleportService;
import me.ihqqq.marriage.util.ItemBuilder;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
public class MarriageMenu extends AbstractMenu {

    private final TeleportService teleportService;
    private final MessageService messages;
    private final FileConfiguration guiConfig;

    public MarriageMenu(@NotNull MarriagePlugin plugin, @NotNull Player viewer) {
        super(plugin, viewer, 54, plugin.getGuiConfig().getString("gui.title", "<gold>Marriage Menu</gold>"));
        this.teleportService = plugin.getTeleportService();
        this.messages = plugin.getMessageService();
        this.guiConfig = plugin.getGuiConfig();
    }

    @Override
    protected void setup() {
        ConfiguredButton infoButton = buildButton("info", 10, Material.BOOK,
                "<aqua>Info</aqua>", List.of("<gray>View your marriage info</gray>"));
        setButton(infoButton.slot(), new MenuButton(infoButton.item(), (event, btn) -> {
            viewer.performCommand("marry info");
        }));
        ConfiguredButton teleportButton = buildButton("teleport", 12, Material.ENDER_PEARL,
                "<green>Teleport</green>", List.of("<gray>Teleport to your partner</gray>"));
        setButton(teleportButton.slot(), new MenuButton(teleportButton.item(), (event, btn) -> {
            teleportService.teleportToPartner(viewer);
        }));
        ConfiguredButton homeButton = buildButton("home", 14, Material.OAK_DOOR,
                "<yellow>Home</yellow>",
                List.of("<gray>Left click to teleport home</gray>", "<gray>Right click to set home</gray>"));
        setButton(homeButton.slot(), new MenuButton(homeButton.item(), (event, btn) -> {
            if (event.getClick().isRightClick()) {
                teleportService.setHome(viewer);
            } else {
                teleportService.teleportHome(viewer);
            }
        }));
        ConfiguredButton chatButton = buildButton("chat", 16, Material.PAPER,
                "<blue>Chat</blue>", List.of("<gray>Toggle marriage chat</gray>"));
        setButton(chatButton.slot(), new MenuButton(chatButton.item(), (event, btn) -> {
            viewer.performCommand("marry chat");
        }));
        ConfiguredButton giftButton = buildButton("gift", 28, Material.CHEST,
                "<gold>Gift</gold>", List.of("<gray>Open the gift menu</gray>"));
        setButton(giftButton.slot(), new MenuButton(giftButton.item(), (event, btn) -> {
            viewer.performCommand("marry gift");
        }));
        ConfiguredButton ringButton = buildButton("ring", 30, Material.GOLD_NUGGET,
                "<light_purple>Ring</light_purple>", List.of("<gray>Manage your ring</gray>"));
        setButton(ringButton.slot(), new MenuButton(ringButton.item(), (event, btn) -> {
            viewer.performCommand("marry ring");
        }));
        ConfiguredButton divorceButton = buildButton("divorce", 32, Material.SHEARS,
                "<red>Divorce</red>", List.of("<gray>Initiate a divorce</gray>"));
        setButton(divorceButton.slot(), new MenuButton(divorceButton.item(), (event, btn) -> {
            viewer.performCommand("marry divorce");
        }));
    }

    @Override
    protected boolean allowItemMove() {
        return false;
    }

    private ConfiguredButton buildButton(@NotNull String key, int defaultSlot, @NotNull Material defaultMaterial,
                                         @NotNull String defaultName, @NotNull List<String> defaultLore) {
        ConfigurationSection section = guiConfig.getConfigurationSection("gui.buttons." + key);
        int slot = section != null ? section.getInt("slot", defaultSlot) : defaultSlot;
        String materialName = section != null ? section.getString("material", defaultMaterial.name()) : defaultMaterial.name();
        Material material = resolveMaterial(materialName, defaultMaterial);
        String name = section != null ? section.getString("name") : null;
        if (name == null || name.isBlank()) {
            String localizedName = messages.getString("gui.buttons." + key + ".name");
            name = localizedName == null ? defaultName : localizedName;
        }
        List<String> lore;
        if (section != null && section.isList("lore")) {
            lore = section.getStringList("lore");
        } else {
            List<String> localizedLore = messages.getStringList("gui.buttons." + key + ".lore");
            lore = localizedLore.isEmpty() ? defaultLore : localizedLore;
        }
        name = applyPlaceholders(name);
        List<String> renderedLore = new ArrayList<>();
        for (String line : lore) {
            renderedLore.add(applyPlaceholders(line));
        }
        ItemBuilder builder = new ItemBuilder(material).name(name);
        if (!renderedLore.isEmpty()) {
            builder.lore(renderedLore);
        }
        return new ConfiguredButton(slot, builder.build());
    }

    private Material resolveMaterial(String materialName, Material fallback) {
        if (materialName == null || materialName.isBlank()) {
            return fallback;
        }
        Material material = Material.matchMaterial(materialName.trim().toUpperCase(Locale.ROOT));
        return material == null ? fallback : material;
    }

    private String applyPlaceholders(@NotNull String input) {
        String result = input;
        boolean hasPartnerPlaceholder = input.contains("%marriage_partner%");
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            result = PlaceholderAPI.setPlaceholders(viewer, input);
        }
        if (hasPartnerPlaceholder) {
            String fallback = messages.getString("placeholders.marriage-partner.single");
            if (fallback != null && !fallback.isBlank()) {
                String stripped = input.replace("%marriage_partner%", "");
                if (result.contains("%marriage_partner%") || result.equals(stripped)) {
                    result = input.replace("%marriage_partner%", fallback);
                }
            }
        }
        return result;
    }

    private record ConfiguredButton(int slot, org.bukkit.inventory.ItemStack item) {
        private ConfiguredButton {
            Objects.requireNonNull(item, "item");
        }
    }
}
