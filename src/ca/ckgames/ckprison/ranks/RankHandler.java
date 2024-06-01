package ca.ckgames.ckprison.ranks;

import ca.ckgames.ckprison.CKPrisonPlugin;
import ca.ckgames.ckprison.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RankHandler implements Listener {
    public CKPrisonPlugin plugin;
    public List<Rank> rankList = new ArrayList<>();

    public RankHandler(CKPrisonPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public List<RankLoadingResult> loadRanks(FileConfiguration configFile) {
        clearRanks();

        List<RankLoadingResult> results = new ArrayList<>();
        ConfigurationSection rankSection = configFile.getConfigurationSection("ranks");

        if (rankSection == null) {
            results.add(new RankLoadingResult(false, "Ranks field is missing or empty in the configuration file."));
            return results;
        }

        for (String rankName : rankSection.getKeys(false)) {
            ConfigurationSection rankConfig = rankSection.getConfigurationSection(rankName);
            if (rankConfig == null) {
                results.add(new RankLoadingResult(false, String.format(
                        "Rank \"%s\" is not valid.",
                        rankName
                )));
                continue;
            }

            int rankupPrice = rankConfig.getInt("rankup_price", 0);
            if (rankupPrice < 0) {
                results.add(new RankLoadingResult(false, String.format(
                        "Rank \"%s\" has an invalid price: %d.",
                        rankName,
                        rankupPrice
                )));
                continue;
            }

            String tag = rankConfig.getString("tag", null);
            String inheritsFromName = rankConfig.getString("inherits", null);
            String nextRankName = rankConfig.getString("next_rank", null);
            List<String> onRankupCommands = rankConfig.getStringList("rankup_commands");

            Rank rank = new Rank(rankName, rankupPrice, tag, inheritsFromName, nextRankName, onRankupCommands);
            rankList.add(rank);
        }

        for (Rank rank : rankList) {
            // Attempt to set rank pointers.
            // Interesting comment man, this isn't C anymore

            Optional<Rank> inheritsFrom = rankList.stream()
                    .filter(r -> Objects.equals(r.name, rank.inheritsFromName))
                    .findFirst();

            inheritsFrom.ifPresentOrElse(
                    r -> rank.inheritsFrom = r,
                    () -> rank.inheritsFrom = handleSpecialRankNameCases(rank, rank.inheritsFromName)
            );

            Optional<Rank> nextRank = rankList.stream()
                    .filter(r -> Objects.equals(r.name, rank.nextRankName))
                    .findFirst();

            nextRank.ifPresentOrElse(
                    r -> rank.nextRank = r,
                    () -> rank.nextRank = handleSpecialRankNameCases(rank, rank.nextRankName)
            );
        }

        plugin.getLogger().info(String.format("Loaded %d rank(s): %d warnings found.", rankList.size(), results.size()));
        return results;
    }

    private Rank handleSpecialRankNameCases(Rank rank, String name) {
        // In the case where there is a rank with one of these special keywords,
        // the rank itself will take priority.

        if ("previous".equals(name)) {
            // Use the previous rank if possible.
            int index = rankList.indexOf(rank) - 1;
            if (index >= 0) {
                return rankList.get(index);
            }
        }

        if ("next".equals(name)) {
            // Use the next rank if possible.
            int index = rankList.indexOf(rank) + 1;
            if (index < rankList.size()) {
                return rankList.get(index);
            }
        }

        return null;
    }

    public Rank getRank(String name) {
        for (Rank rank : rankList) {
            if (rank.name.equals(name)) {
                return rank;
            }
        }
        return null;
    }

    public void clearRanks() {
        rankList.clear();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String rankName = plugin.databaseHandler.getPlayerRank(event.getPlayer());
        if (rankName == null) {
            return;
        }
        Rank rank = getRank(rankName);
        if (rank == null) {
            return;
        }
        event.setFormat(event.getFormat().replace("{prison_rank}", Utils.format(rank.tag)));
    }
}
