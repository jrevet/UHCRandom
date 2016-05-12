package net.samagames.uhcrandom;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.gui.AbstractGui;
import net.samagames.api.gui.IGuiManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RandomGUI extends AbstractGui
{
    public static final String INVNAME = "UHCRandom";

    private List<RandomModule> modules;
    private Callback callback;
    private UHCRandom plugin;
    private int enabled;

    private int[] delays = new int[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 5, 5, 6, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 25, 10};
    private short[] colors = new short[]{0, 1, 2, 3, 4, 5, 6};
    private int index;

    public RandomGUI(UHCRandom plugin, Collection<RandomModule> allModules, Collection<RandomModule> enabledModules, Callback callback)
    {
        this.plugin = plugin;
        this.modules = new ArrayList<>();
        this.enabled = enabledModules.size();
        Iterator<RandomModule> it = allModules.iterator();
        for (int i = 0; i < this.delays.length; i++)
        {
            if (!it.hasNext())
                it = allModules.iterator();
            if (it.hasNext())
                this.modules.add(it.next());
        }
        this.modules.addAll(enabledModules);
        this.callback = callback;
        this.index = 0;
        this.next();
    }

    /**
     * Open inventory if not opened (first time or after close).
     * @param player The player holding the inventory.
     */
    @Override
    public void display(Player player)
    {
        player.closeInventory();
        player.openInventory(this.inventory);
    }

    /**
     * Generates next inventory, with random glass colors (without light grey), and modules icons.
     */
    private void next()
    {
        Random random = new Random();
        int size = 18 + (((this.enabled - 1) / 7 + 1) * 9);
        this.inventory = Bukkit.createInventory(null, size, INVNAME);
        int j = 0;
        for (int i = 0; i < size; i++)
        {
            if (i < 9 || i > size - 9 || i % 9 < 1 || i % 9 > 7)
                this.setSlotData(" ", new ItemStack(Material.STAINED_GLASS_PANE, 1, this.colors[random.nextInt(this.colors.length)]), i, null, "");
            else if (j >= this.enabled)
                this.setSlotData(" ", new ItemStack(Material.STAINED_GLASS_PANE, 1, this.colors[random.nextInt(this.colors.length)]), i, null, "");//F**k U Sonar
            else
            {
                RandomModule module = this.modules.get(j);
                this.setSlotData(ChatColor.BOLD + "" + ChatColor.AQUA + module.getName(), module.getItem(), i, new String[]{ChatColor.GRAY + module.getDescription()}, "");
                j++;
            }
        }
        this.modules.remove(0);
        this.nextNext();
        this.index++;
    }

    /**
     * Seperated method.
     * HELLO SONAR, HOW ARE YOU ?
     */
    private void nextNext()
    {
        IGuiManager manager = SamaGamesAPI.get().getGuiManager();
        for (Player player : this.plugin.getServer().getOnlinePlayers())
        {
            player.playSound(player.getLocation(), this.index < this.delays.length ? Sound.ENTITY_EXPERIENCE_ORB_PICKUP : Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
            InventoryView view = player.getOpenInventory();
            if (view == null || view.getTopInventory() == null || !view.getTopInventory().getName().equals(INVNAME))
                manager.openGui(player, this);
            else
                view.getTopInventory().setContents(this.inventory.getContents());
        }
        if (this.index < this.delays.length)
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, this::next, this.delays[this.index]);
        else
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                this.plugin.getServer().getOnlinePlayers().forEach(Player::closeInventory);
                this.callback.run();
            }, 240);
    }
}
