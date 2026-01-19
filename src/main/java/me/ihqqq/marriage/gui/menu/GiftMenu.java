package me.ihqqq.marriage.gui.menu;

import me.ihqqq.marriage.MarriagePlugin;
import me.ihqqq.marriage.gui.component.MenuButton;
import me.ihqqq.marriage.model.MarriageRecord;
import me.ihqqq.marriage.service.SharedInventoryService;
import me.ihqqq.marriage.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;


public class GiftMenu extends AbstractMenu {

    public static final String TITLE = "<gold>Gift Menu</gold>";
    private static final int BACK_SLOT = 49;
    private static final Set<Integer> RESERVED_SLOTS = Set.of(BACK_SLOT);

    private final MarriageRecord record;
    private final SharedInventoryService sharedInventoryService;

    public GiftMenu(@NotNull MarriagePlugin plugin, @NotNull Player viewer,
                    @NotNull MarriageRecord record,
                    @NotNull org.bukkit.inventory.Inventory inventory) {
        super(plugin, viewer, 54, TITLE);
        this.record = record;
        this.sharedInventoryService = plugin.getSharedInventoryService();
        setInventory(inventory);
    }

    @Override
    protected void setup() {
        setButton(BACK_SLOT, new MenuButton(
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

    @Override
    protected boolean allowItemMove() {
        return true;
    }

    @Override
    public void handleClose(@NotNull InventoryCloseEvent event) {
        sharedInventoryService.saveInventory(record, getInventory(), RESERVED_SLOTS);
    }
}
