package ca.ckgames.ckprison.commands;

import ca.ckgames.ckprison.SQLiteHandler;
import ca.ckgames.ckprison.Utils;
import ca.ckgames.ckprison.ranks.Rank;
import ca.ckgames.ckprison.ranks.RankHandler;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Rankup implements CommandExecutor {
    SQLiteHandler databaseHandler;
    RankHandler rankHandler;
    Economy economy;

    public Rankup(SQLiteHandler databaseHandler, RankHandler rankHandler, Economy economy) {
        this.databaseHandler = databaseHandler;
        this.rankHandler = rankHandler;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use the rankup command.");
            return true;
        }

        if (economy == null) {
            // This command will not work if this is the case.
            sender.sendMessage(ChatColor.RED + "This command is not functional currently.");
            if (Utils.isAdmin(player)) {
                sender.sendMessage(ChatColor.GRAY + "ADMIN: Vault & an economy plugin (ie. EssentialsX) needs to be installed on the server in order to run this command!");
            }
            return true;
        }

        String rankName = databaseHandler.getPlayerRank(player);
        Rank rank = rankHandler.getRank(rankName);

        if (rank == null) {
            sender.sendMessage(ChatColor.RED + "You do not have a valid rank!");
            return true;
        }

        if (rank.nextRank == null) {
            sender.sendMessage(ChatColor.RED + "There is no rank to rankup to!");
            return true;
        }

        double balance = economy.getBalance(player);

        if (balance < rank.rankupPrice) {
            // Not enough funds to rank up.
            sender.sendMessage(String.format(
                    ChatColor.RED + "You do not have enough currency to rank up! Progress: %d/%d (%.2f%%).",
                    (int) balance,
                    rank.rankupPrice,
                    rank.rankupPrice > 0 ? balance / rank.rankupPrice * 100 : 0
            ));
            return true;
        }

        EconomyResponse response = economy.withdrawPlayer(player, rank.rankupPrice);
        if (!response.transactionSuccess()) {
            sender.sendMessage(ChatColor.RED + "Could not take the currency.");
            if (Utils.isAdmin(player)) {
                sender.sendMessage(ChatColor.GRAY + "ADMIN: Check the server log for more details!");
            }
            Bukkit.getLogger().warning(String.format("Error while taking currency from \"%s\": %s", player.getName(), response.errorMessage));
            return true;
        }

        databaseHandler.setPlayerRank(player, rank.nextRank);
        rank.onRankAchieved(player);
        sender.sendMessage(String.format(ChatColor.GOLD + "Congratulations! You've ranked up! New tag: %s", ChatColor.translateAlternateColorCodes('&', rank.nextRank.tag)));
        return true;
    }
}
