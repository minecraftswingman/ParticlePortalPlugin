package com.pessulum.particleportals.Commands;

import com.pessulum.particleportals.ParticlePortals;
import com.pessulum.particleportals.PortalSpawner.CustomPortals;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TabCompletion implements TabCompleter {

    List<String> arguments = new ArrayList<>();
    ParticlePortals plugin;

    public TabCompletion(ParticlePortals plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (arguments.isEmpty()) {
            arguments.add("create");
            arguments.add("spawn");
            arguments.add("set");
            arguments.add("disband");
            arguments.add("inventory");
        }

        List<String> results = new ArrayList<>();
        List<String> subNameResults = List.of("YourPortalName");
        List<String> subKeyResults = List.of("YourKey");

        List<String> locations = List.of("home", "destination");
        List<String> portalLocationSet = List.of("current", "click");
        List<String> free = List.of("free", "current", "click");

        List<String> shapeTypes = Arrays.stream(CustomPortals.values())
                .map(CustomPortals::getShapeName)
                .collect(Collectors.toList());

        // /portal <arg>
        if (args.length == 1) {
            for (String a : arguments) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
                    results.add(a);
                }
            }
            return results;
        }


        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "create":
                    return subNameResults;
                case "spawn":
                case "disband":
                    return subKeyResults;
                case "set":
                    return List.of("home", "destination", "shape");
            }
        }


        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("create")) {
                return subKeyResults;
            } else if (args[0].equalsIgnoreCase("set")) {
                switch (args[1].toLowerCase()) {
                    case "home":
                        return free;
                    case "destination":
                        return portalLocationSet;
                    case "shape":
                        return shapeTypes;
                }
            }
        }


        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("shape")) {
                return subKeyResults;
            } else if (args[0].equalsIgnoreCase("set")) {
                return subKeyResults;
            }
        }

        return null;
    }
}