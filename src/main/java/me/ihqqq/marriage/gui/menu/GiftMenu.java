package me.ihqqq.marriage.gui.menu;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.gui.component.MenuButton;
import me.ihqqq.marriage.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class GiftMenu extends AbstractMenu {

    public GiftMenu(@NotNull MarriagePlugin plugin, @NotNull Player viewer) {
        super(plugin, viewer, 54, "<gold>Gift Menu</gold>");
    }

    @Override
    protected void setup() {
        setButton(49, new MenuButton(
                new ItemBuilder(Material.BARRIER)
                        .name("<red>Close</red>")
                        .lore(List.of("<gray>Return to the marriage menu</gray>"))
                        .build(),
                (event, btn) -> {
                    Player player = (event.getWhoClicked() instanceof Player p) ? p : viewer;
                    player.closeInventory();
                    
                    plugin.getSchedulerUtil().runSyncLater(() -> plugin.getMenuManager()
                            .openMenu(player, new MarriageMenu(plugin, player)), 1L);
                }
        ));
    }
}
