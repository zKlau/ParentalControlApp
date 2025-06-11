package Processes;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * The {@code WebFilter} class provides methods to block and unblock websites
 * by modifying the system's hosts file. It is used to enforce web restrictions
 * for users in the Parental Control App.
 * <p>
 * This class supports blocking/unblocking individual sites or lists of sites,
 * and automatically detects the correct hosts file path for Windows, macOS, and Linux.
 * </p>
 *
 * @author Claudiu Padure
 * @version 1.0
 */
public class WebFilter {

    /**
     * The path to the system's hosts file.
     */
    private final String hostsFile;

    /**
     * Constructs a {@code WebFilter} and determines the correct hosts file path
     * based on the operating system.
     */
    public WebFilter() {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win")) {
            hostsFile = "C:\\Windows\\System32\\drivers\\etc\\hosts";
        } else if (OS.contains("mac") || OS.contains("nux")) {
            hostsFile = "/etc/hosts";
        } else {
            throw new RuntimeException("Unsupported OS: cannot modify hosts file.");
        }
    }

    /**
     * Blocks a website by adding entries to the hosts file.
     *
     * @param rawUrl The URL or domain to block.
     */
    public void blockSite(String rawUrl) {
        try {
            String url = normalizeDomain(rawUrl);
            List<String> lines = Files.readAllLines(Paths.get(hostsFile));

            String entry1 = System.lineSeparator() + "127.0.0.1 " + url;
            String entry2 = "127.0.0.1 www." + url;

            boolean alreadyBlocked = lines.stream().anyMatch(line ->
                    line.trim().equals(entry1) || line.trim().equals(entry2));

            if (!alreadyBlocked) {
                List<String> toAdd = new ArrayList<>();
                toAdd.add(entry1);
                toAdd.add(entry2);
                Files.write(Paths.get(hostsFile), toAdd, StandardOpenOption.APPEND);
                System.out.println("Blocked: " + rawUrl);
            } else {
                System.out.println("Already blocked: " + rawUrl);
            }
        } catch (IOException e) {
            throw new RuntimeException("Access denied to hosts file. Run as administrator.", e);
        }
    }

    /**
     * Unblocks a website by removing its entries from the hosts file.
     *
     * @param rawUrl The URL or domain to unblock.
     */
    public void unblockSite(String rawUrl) {
        try {
            String url = normalizeDomain(rawUrl);
            Path path = Paths.get(hostsFile);
            List<String> lines = new ArrayList<>(Files.readAllLines(path));

            for (String i : lines) {
                String line = i.trim();
                if (i.equals("127.0.0.1 " + url) || line.equals("127.0.0.1 www." + url)) {
                    lines.remove(i);
                    System.out.println("Unblocked: " + rawUrl);
                }
            }

            Files.write(path, lines, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Access denied to hosts file. Run as administrator.", e);
        }
    }

    /**
     * Unblocks a list of websites by removing their entries from the hosts file.
     *
     * @param processList The list of {@link ProcessInfo} objects representing sites to unblock.
     */
    public void unblockSites(List<ProcessInfo> processList) {
        try {
            System.out.println("Unblocking WEBSITES " + processList.size() + " entries");
            Path path = Paths.get(hostsFile);
            List<String> lines = new ArrayList<>(Files.readAllLines(path));
            Set<String> domainsToUnblock = new HashSet<>();

            for (ProcessInfo p : processList) {
                String url = normalizeDomain(p.getProcess_name());
                domainsToUnblock.add("127.0.0.1 " + url);
                domainsToUnblock.add("127.0.0.1 www." + url);
            }

            lines.removeIf(line -> domainsToUnblock.contains(line.trim()));

            Files.write(path, lines, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Websites Unblocked: " + processList.size() + " entries");
        } catch (IOException e) {
            throw new RuntimeException("Access denied to hosts file. Run as administrator.", e);
        }
    }

    /**
     * Normalizes a URL or domain by removing protocol, "www.", and path components.
     *
     * @param url The raw URL or domain.
     * @return The normalized domain.
     */
    private String normalizeDomain(String url) {
        if (url == null || url.isEmpty()) return "";

        url = url.trim().toLowerCase();


        if (url.startsWith("http://")) url = url.substring(7);
        else if (url.startsWith("https://")) url = url.substring(8);


        int slashIndex = url.indexOf('/');
        if (slashIndex != -1) url = url.substring(0, slashIndex);

        if (url.startsWith("www.")) url = url.substring(4);

        return url;
    }
}
