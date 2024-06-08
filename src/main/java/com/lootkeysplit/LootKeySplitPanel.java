package com.lootkeysplit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.client.ui.PluginPanel;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.lootkeysplit.LootKeySplitPlugin;


@Slf4j
public class LootKeySplitPanel extends PluginPanel
{
    public static String logfilepath = RuneLite.RUNELITE_DIR + "//chatlogs//clan//latest.log";

    JLabel timerlabel;
    Timer timer;
    int count;

    private final LootKeySplitPlugin plugin;
    private final LootKeySplitConfig config;

    private final JPanel contentPanel;

    private ResetButton startButton;
    private ResetButton endButton;
    private ResetButton calcLoot;
    private ResetButton resetButton;
    private ResetButton restartTimer;
    private long startTime;
    private long elapsedTime;
    private JLabel timerLabel = null;
    private JTextArea createTextArea(String text)
    {
        JTextArea textArea = new JTextArea(5, 20);
        textArea.setText(text);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setOpaque(false);

        return textArea;
    }

    @Inject
    public LootKeySplitPanel(LootKeySplitPlugin plugin, LootKeySplitConfig config) {
        super(false);
        this.plugin = plugin;
        this.config = config;

        JLabel title = new JLabel("LOOT SPLIT");
        title.setBorder(new EmptyBorder(0, 0, BORDER_OFFSET, 0));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel = new JPanel();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(BORDER_OFFSET, BORDER_OFFSET, BORDER_OFFSET, BORDER_OFFSET));
        add(title, BorderLayout.NORTH);

        add(contentPanel, BorderLayout.CENTER);

        startButton = new ResetButton("START TIMER");
        startButton.setPreferredSize(new Dimension(PANEL_WIDTH, 30));
        contentPanel.add(startButton);

        endButton = new ResetButton("END TIMER");
        endButton.setPreferredSize(new Dimension(PANEL_WIDTH, 30));
        contentPanel.add(endButton);

        restartTimer = new ResetButton("RESET TIMER");
        restartTimer.setPreferredSize(new Dimension(PANEL_WIDTH, 30));
        contentPanel.add(restartTimer);

        timerLabel = new JLabel("00:00:00");
        contentPanel.add(timerLabel);

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateElapsedTime();
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startTimer();
            }
        });

        endButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopTimer();

            }
        });

        restartTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetTimer();
            }
        });

        JTextArea textArea;
        textArea = new JTextArea(20, 20);
        textArea.setEditable(false);
        contentPanel.add(textArea, BorderLayout.CENTER);

        calcLoot = new ResetButton("SPLIT LOOT");
        calcLoot.setPreferredSize(new Dimension(PANEL_WIDTH, 30));

        calcLoot.addActionListener(e -> openFile(logfilepath, textArea)); //logfilepath "C:\\Users\\keisl\\.runelite\\chatlogs\\clan\\latest.log"
        contentPanel.add(calcLoot);

        resetButton = new ResetButton("RESET KEY LOOT");
        resetButton.setPreferredSize(new Dimension(PANEL_WIDTH, 30));
        contentPanel.add(resetButton);

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    plugin.resetChatLog();
                    textArea.setText("LOOT KEY LOG CLEARED");

                    // Schedule a task to clear the text area after 5 seconds
                    Timer timer = new Timer(3000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            textArea.setText(""); // Clear the text area
                        }
                    });
                    timer.setRepeats(false); // Set the timer to execute only once
                    timer.start();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });


    }

    private void startTimer() {
        startTime = System.currentTimeMillis() - elapsedTime;
        timer.start();
        SharedState.isLoggingEnabled = true;
    }

    private void stopTimer() {
        timer.stop();
        elapsedTime = System.currentTimeMillis() - startTime;
        SharedState.isLoggingEnabled = false;
    }

    private void resetTimer() {
        timer.stop();
        startTime = 0;
        elapsedTime = 0;
        timerLabel.setText("00:00:00");
        SharedState.isLoggingEnabled = false; // Disable logging when the timer resets
    }

    private void updateElapsedTime() {
        long now = System.currentTimeMillis();
        long elapsed = now - startTime;

        long hours = (elapsed / 3600000) % 24;
        long minutes = (elapsed / 60000) % 60;
        long seconds = (elapsed / 1000) % 60;

        timerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }


    private void openFile(String filePath, JTextArea textArea) {
        BufferedReader reader = null;
        try {

            System.out.println("File exists: " + filePath); // Debugging statement

            reader = new BufferedReader(new FileReader(filePath));
            StringBuilder content = new StringBuilder();
            Map<String, Integer> userCoinsMap = new HashMap<>(); // Map to store username and total coins
            int totalCoins = 0;
            int uniquePlayersCount = 0; // Counter for unique players
            String[] uniquePlayers = new String[5]; // Array to store unique player names
            Arrays.fill(uniquePlayers, ""); // Initialize the array with empty strings

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("opened a loot key worth")) {
                    String[] parts = line.split("\\s+"); // Split the line by whitespace
                    if (parts.length >= 5) { // Ensure there are enough parts
                        String username = parts[0]; // First word is the username
                        String coinsStr = parts[parts.length - 2]; // Second to last string is the coins amount
                        int coins = Integer.parseInt(coinsStr.replace(",", "")); // Parse coins string to integer
                        userCoinsMap.put(username, userCoinsMap.getOrDefault(username, 0) + coins); // Update total coins for the user
                        totalCoins += coins;
                        if (!contains(uniquePlayers, username)) {
                            uniquePlayers[uniquePlayersCount++] = username; // Add the player to the array if not already present
                        }
                    }
                }
            }

            // Calculate average payout
            int numberOfPlayers = uniquePlayersCount;
            int averagePayout = totalCoins / numberOfPlayers;

            // Build the output string with formatted coin amounts
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setGroupingUsed(true);
            String formattedAveragePayout = numberFormat.format(averagePayout);
            String formattedTotalPayout = numberFormat.format(totalCoins);

            // Append individual player payouts
            for (int i = 0; i < uniquePlayersCount; i++) {
                String username = uniquePlayers[i];
                int coins = userCoinsMap.get(username);
                String formattedCoins = numberFormat.format(coins);
                content.append(username).append(" -- ").append(formattedCoins).append("\n");
            }
            content.append("____________________").append("\n");
            content.append("TOTAL LOOTED: ").append(formattedTotalPayout).append(" coins!\n");
            content.append("Payout Each : ").append(formattedAveragePayout).append(" coins!\n");

            textArea.setText(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean contains(String[] array, String value) {
        for (String str : array) {
            if (str.equals(value)) {
                return true;
            }
        }
        return false;
    }





}