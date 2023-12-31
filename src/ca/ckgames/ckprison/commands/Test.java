package ca.ckgames.ckprison.commands;

import ca.ckgames.ckprison.SQLiteHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Test implements CommandExecutor {
    SQLiteHandler databaseHandler;

    public Test(SQLiteHandler s) {
        databaseHandler = s;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            String rank = databaseHandler.getPlayerRank(player);
            player.sendMessage(String.format("Current rank: %s", rank));
            player.showDemoScreen();
        }
        return true;
    }
}
