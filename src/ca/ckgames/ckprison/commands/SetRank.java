package ca.ckgames.ckprison.commands;

import ca.ckgames.ckprison.SQLiteHandler;
import ca.ckgames.ckprison.Utils;
import ca.ckgames.ckprison.ranks.Rank;
import ca.ckgames.ckprison.ranks.RankHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetRank implements CommandExecutor {
    private final SQLiteHandler databaseHandler;
    private final RankHandler rankHandler;

    public SetRank(SQLiteHandler databaseHandler, RankHandler rankHandler) {
        this.databaseHandler = databaseHandler;
        this.rankHandler = rankHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /setrank [player_name] [rank_name]");
            return true;
        }

        String playerName = args[0];
        String rankName = args[1];

        Rank rank = rankHandler.getRank(rankName);
        if (rank == null) {
            sender.sendMessage(Utils.format(ChatColor.RED + "Rank %s does not exist.", rankName));
            return true;
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(Utils.format(ChatColor.RED + "Could not find player with name %s.", playerName));
            return true;
        }

        databaseHandler.setPlayerRank(player, rank);
        rank.onRankAchieved(player);
        sender.sendMessage(Utils.format( ChatColor.GREEN + "Set %s's rank to %s.", player.getName(), rank.name));
        return true;
    }
}
