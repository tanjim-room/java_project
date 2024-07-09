import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ShutTheBox extends JFrame {
    private JButton[][] playerTiles;
    private JButton rollButton;
    private JButton confirmButton;
    private JLabel diceLabel;
    private JLabel playerLabel;
    private int currentPlayer;
    private int dice1, dice2;
    private int numPlayers;
    private String[] playerNames;
    private boolean[][] tileLocked;
    private Set<Integer> selectedTiles;
    private int selectedSum;

    public ShutTheBox() {
        showPlayerNumberDialog();
    }

    private void showPlayerNumberDialog() {
        JDialog playerNumberDialog = new JDialog(this, "Number of Players", true);
        playerNumberDialog.setSize(300, 150);
        playerNumberDialog.setLayout(new GridLayout(3, 1));

        JLabel playerCountLabel = new JLabel("Number of players (2-4):");
        JTextField playerCountField = new JTextField();

        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(e -> {
            try {
                numPlayers = Integer.parseInt(playerCountField.getText());
                if (numPlayers < 2 || numPlayers > 4) {
                    throw new NumberFormatException();
                }
                playerNumberDialog.dispose();
                showPlayerNameDialog();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(playerNumberDialog, "Please enter a valid number of players (2-4).");
            }
        });

        playerNumberDialog.add(playerCountLabel);
        playerNumberDialog.add(playerCountField);
        playerNumberDialog.add(nextButton);
        playerNumberDialog.setLocationRelativeTo(null);  // Center the dialog
        playerNumberDialog.setVisible(true);
    }

    private void showPlayerNameDialog() {
        JDialog playerNameDialog = new JDialog(this, "Player Names", true);
        playerNameDialog.setSize(300, 150 + 50 * numPlayers);
        playerNameDialog.setLayout(new GridLayout(numPlayers + 1, 2));

        playerNames = new String[numPlayers];
        JTextField[] nameFields = new JTextField[numPlayers];

        for (int i = 0; i < numPlayers; i++) {
            JLabel nameLabel = new JLabel("Player " + (i + 1) + " name:");
            nameFields[i] = new JTextField();
            playerNameDialog.add(nameLabel);
            playerNameDialog.add(nameFields[i]);
        }

        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(e -> {
            for (int i = 0; i < numPlayers; i++) {
                playerNames[i] = nameFields[i].getText();
                if (playerNames[i].isEmpty()) {
                    JOptionPane.showMessageDialog(playerNameDialog, "Please enter a name for Player " + (i + 1) + ".");
                    return;
                }
            }
            playerNameDialog.dispose();
            initUI();
        });

        playerNameDialog.add(new JLabel());
        playerNameDialog.add(startButton);
        playerNameDialog.setLocationRelativeTo(null);  // Center the dialog
        playerNameDialog.setVisible(true);
    }

    private void initUI() {
        currentPlayer = 0;  // Start with the first player
        tileLocked = new boolean[numPlayers][10];
        selectedTiles = new HashSet<>();
        selectedSum = 0;

        setTitle("Shut the Box");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Main panel with padding and margin
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));  // Add padding around the panel

        JPanel tilePanel = new JPanel();
        tilePanel.setLayout(new GridLayout(numPlayers, 10, 5, 5)); // Added margin between buttons
        playerTiles = new JButton[numPlayers][10];

        for (int i = 0; i < numPlayers; i++) {
            for (int j = 0; j < 10; j++) {
                playerTiles[i][j] = new JButton(String.valueOf(j + 1));
                playerTiles[i][j].addActionListener(new TileListener(i, j + 1));
                playerTiles[i][j].setEnabled(false);
                tilePanel.add(playerTiles[i][j]);
            }
        }

        rollButton = new JButton("Roll Dice");
        rollButton.addActionListener(new RollListener());

        confirmButton = new JButton("Confirm Selection");
        confirmButton.addActionListener(new ConfirmListener());
        confirmButton.setEnabled(false);

        diceLabel = new JLabel("Dice: ");
        playerLabel = new JLabel(playerNames[currentPlayer] + "'s turn");

        JPanel controlPanel = new JPanel();
        controlPanel.add(rollButton);
        controlPanel.add(diceLabel);
        controlPanel.add(playerLabel);
        controlPanel.add(confirmButton);

        mainPanel.add(tilePanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setLocationRelativeTo(null);  // Center the main frame
        setVisible(true);

        enablePlayerTiles(currentPlayer);
    }

    private void rollDice() {
        Random rand = new Random();
        dice1 = rand.nextInt(6) + 1;
        dice2 = rand.nextInt(6) + 1;
        diceLabel.setText("Dice: " + dice1 + " + " + dice2 + " = " + (dice1 + dice2));
        confirmButton.setEnabled(true); // Enable confirm button after rolling dice
        rollButton.setEnabled(false); // Disable roll button after rolling dice

        if (!canMatchDiceSum(dice1 + dice2)) {
            JOptionPane.showMessageDialog(ShutTheBox.this, playerNames[currentPlayer] + " cannot match the dice sum. Next player's turn.");
            switchToNextPlayer();
        }
    }

    private boolean canMatchDiceSum(int diceSum) {
        return canMatchSum(diceSum, 0);
    }

    private boolean canMatchSum(int sum, int startIndex) {
        if (sum == 0) return true;
        if (sum < 0) return false;
        for (int i = startIndex; i < 10; i++) {
            if (!tileLocked[currentPlayer][i] && canMatchSum(sum - (i + 1), i + 1)) {
                return true;
            }
        }
        return false;
    }

    private void enablePlayerTiles(int playerIndex) {
        for (int i = 0; i < numPlayers; i++) {
            for (int j = 0; j < 10; j++) {
                playerTiles[i][j].setEnabled(i == playerIndex && !tileLocked[i][j]);
            }
        }
    }

    private void lockSelectedTiles() {
        for (int tileNumber : selectedTiles) {
            playerTiles[currentPlayer][tileNumber - 1].setEnabled(false);
            tileLocked[currentPlayer][tileNumber - 1] = true;
        }
        selectedTiles.clear();
        selectedSum = 0;
        confirmButton.setEnabled(false);

        switchToNextPlayer();
    }

    private void switchToNextPlayer() {
        currentPlayer = (currentPlayer + 1) % numPlayers;
        playerLabel.setText(playerNames[currentPlayer] + "'s turn");
        rollButton.setEnabled(true); // Enable roll button at the start of the next turn
        enablePlayerTiles(currentPlayer);
        checkForWinner();
    }

    private void checkForWinner() {
        for (int i = 0; i < numPlayers; i++) {
            boolean allTilesClosed = true;
            for (int j = 0; j < 10; j++) {
                if (!tileLocked[i][j]) {
                    allTilesClosed = false;
                    break;
                }
            }
            if (allTilesClosed) {
                JOptionPane.showMessageDialog(this, playerNames[i] + " has closed all their tiles and is the winner!");
                System.exit(0);
            }
        }
    }

    private class RollListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            rollDice();
            enablePlayerTiles(currentPlayer);
            selectedTiles.clear();
            selectedSum = 0;
        }
    }

    private class TileListener implements ActionListener {
        private int playerIndex;
        private int tileNumber;

        public TileListener(int playerIndex, int tileNumber) {
            this.playerIndex = playerIndex;
            this.tileNumber = tileNumber;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!tileLocked[playerIndex][tileNumber - 1]) {
                if (selectedTiles.contains(tileNumber)) {
                    selectedTiles.remove(tileNumber);
                    selectedSum -= tileNumber;
                    playerTiles[playerIndex][tileNumber - 1].setEnabled(true); // Deselect tile
                } else {
                    selectedTiles.add(tileNumber);
                    selectedSum += tileNumber;
                    playerTiles[playerIndex][tileNumber - 1].setEnabled(false); // Select tile
                }
            }
        }
    }

    private class ConfirmListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedSum == (dice1 + dice2)) {
                lockSelectedTiles();
            } else {
                JOptionPane.showMessageDialog(ShutTheBox.this, "Selected tiles do not sum to the dice roll. Next player's turn.");
                // Reset selection
                for (int tileNumber : selectedTiles) {
                    playerTiles[currentPlayer][tileNumber - 1].setEnabled(true);
                }
                selectedTiles.clear();
                selectedSum = 0;
                switchToNextPlayer();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ShutTheBox());
    }
}
