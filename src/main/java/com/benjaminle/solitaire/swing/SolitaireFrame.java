package com.benjaminle.solitaire.swing;

import com.benjaminle.solitaire.engine.Card;
import com.benjaminle.solitaire.engine.KlondikeGame;
import com.benjaminle.solitaire.engine.MoveResult;
import com.benjaminle.solitaire.engine.Suit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Main window for the Swing edition.
 *
 * <p>Interaction is deliberately click-based: select a waste, foundation, or
 * face-up tableau card, then click the destination. This remains accessible on
 * touchpads and avoids hiding legal-move feedback inside drag-and-drop code.</p>
 */
final class SolitaireFrame extends JFrame {
    private static final Color FELT = new Color(20, 105, 64);
    private static final Color FELT_DARK = new Color(12, 74, 44);
    private static final int CARD_WIDTH = 92;
    private static final int CARD_HEIGHT = 126;
    private static final int COLUMN_GAP = 22;
    private static final int FACE_DOWN_OFFSET = 22;
    private static final int FACE_UP_OFFSET = 34;

    private KlondikeGame game = new KlondikeGame();
    private final JPanel topPiles = new JPanel(null);
    private final JPanel tableauArea = new JPanel(null);
    private final JLabel status = new JLabel("Click the stock to draw, or select a card to move.");
    private Selection selection;

    SolitaireFrame() {
        super("Klondike Solitaire");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 650));
        setSize(1020, 760);
        setLocationByPlatform(true);
        createMenuBar();
        createLayout();
        refreshBoard();
    }

    private void createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setMnemonic(KeyEvent.VK_G);

        JMenuItem newGame = new JMenuItem("New Game");
        newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newGame.addActionListener(event -> newGame());
        gameMenu.add(newGame);

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(event -> dispose());
        gameMenu.addSeparator();
        gameMenu.add(exit);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem instructions = new JMenuItem("How to Play");
        instructions.addActionListener(event -> showInstructions());
        helpMenu.add(instructions);

        bar.add(gameMenu);
        bar.add(helpMenu);
        setJMenuBar(bar);
    }

    private void createLayout() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(FELT);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        topPiles.setBackground(FELT);
        topPiles.setPreferredSize(new Dimension(900, CARD_HEIGHT + 20));
        root.add(topPiles, BorderLayout.NORTH);

        tableauArea.setBackground(FELT);
        JScrollPane scrollPane = new JScrollPane(tableauArea);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(FELT);
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);
        root.add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        status.setForeground(Color.WHITE);
        status.setFont(status.getFont().deriveFont(Font.BOLD));
        footer.add(status, BorderLayout.CENTER);
        JButton newButton = new JButton("New Game");
        newButton.addActionListener(event -> newGame());
        footer.add(newButton, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);
    }

    /** Rebuilds visual components from the authoritative engine state. */
    private void refreshBoard() {
        topPiles.removeAll();
        tableauArea.removeAll();
        buildTopPiles();
        buildTableau();
        topPiles.revalidate();
        topPiles.repaint();
        tableauArea.revalidate();
        tableauArea.repaint();
    }

    private void buildTopPiles() {
        int y = 4;
        JComponent stock = game.stockSize() > 0
                ? CardView.faceDown(game.stockSize() + "", this::draw)
                : EmptyPileView.stock(this::draw);
        stock.setBounds(8, y, CARD_WIDTH, CARD_HEIGHT);
        topPiles.add(stock);

        Card waste = game.wasteTop().orElse(null);
        JComponent wasteView = waste == null
                ? new EmptyPileView("Waste", null)
                : new CardView(waste, isSelected(SelectionType.WASTE, 0, 0, null), this::selectWaste);
        wasteView.setBounds(8 + CARD_WIDTH + COLUMN_GAP, y, CARD_WIDTH, CARD_HEIGHT);
        topPiles.add(wasteView);

        int foundationStart = 8 + (CARD_WIDTH + COLUMN_GAP) * 3;
        int index = 0;
        for (Suit suit : Suit.values()) {
            List<Card> pile = game.foundation(suit);
            Card top = pile.isEmpty() ? null : pile.get(pile.size() - 1);
            Runnable click = () -> clickFoundation(suit);
            JComponent view = top == null
                    ? new EmptyPileView(String.valueOf(suit.symbol()), click)
                    : new CardView(top, isSelected(SelectionType.FOUNDATION, 0, 0, suit), click);
            view.setBounds(foundationStart + index * (CARD_WIDTH + COLUMN_GAP), y, CARD_WIDTH, CARD_HEIGHT);
            topPiles.add(view);
            index++;
        }
    }

    private void buildTableau() {
        int largestBottom = CARD_HEIGHT;
        for (int column = 0; column < KlondikeGame.TABLEAU_COUNT; column++) {
            int selectedColumn = column;
            int x = 8 + column * (CARD_WIDTH + COLUMN_GAP);
            List<Card> cards = game.tableau(column);
            if (cards.isEmpty()) {
                EmptyPileView empty = new EmptyPileView("K", () -> clickTableauDestination(selectedColumn));
                empty.setBounds(x, 8, CARD_WIDTH, CARD_HEIGHT);
                tableauArea.add(empty);
                continue;
            }

            int y = 8;
            for (int cardIndex = 0; cardIndex < cards.size(); cardIndex++) {
                Card card = cards.get(cardIndex);
                int selectedIndex = cardIndex;
                Runnable click = () -> clickTableauCard(selectedColumn, selectedIndex);
                boolean selected = isSelected(SelectionType.TABLEAU, column, cardIndex, null);
                CardView view = new CardView(card, selected, click);
                view.setBounds(x, y, CARD_WIDTH, CARD_HEIGHT);
                tableauArea.add(view);
                tableauArea.setComponentZOrder(view, 0);
                if (cardIndex < cards.size() - 1) {
                    y += card.isFaceUp() ? FACE_UP_OFFSET : FACE_DOWN_OFFSET;
                }
            }
            largestBottom = Math.max(largestBottom, y + CARD_HEIGHT);
        }
        int width = 16 + KlondikeGame.TABLEAU_COUNT * (CARD_WIDTH + COLUMN_GAP);
        tableauArea.setPreferredSize(new Dimension(width, largestBottom + 20));
    }

    private void draw() {
        selection = null;
        report(game.draw());
    }

    private void selectWaste() {
        selection = new Selection(SelectionType.WASTE, -1, -1, null);
        setStatus("Selected waste card. Click a tableau or foundation destination.", true);
        refreshBoard();
    }

    private void clickTableauCard(int column, int cardIndex) {
        Card clicked = game.tableau(column).get(cardIndex);
        if (selection == null) {
            if (!clicked.isFaceUp()) {
                setStatus("Face-down cards cannot be selected.", false);
                return;
            }
            selection = new Selection(SelectionType.TABLEAU, column, cardIndex, null);
            setStatus("Selected " + clicked.shortName() + " and the cards below it.", true);
            refreshBoard();
            return;
        }
        if (selection.type == SelectionType.TABLEAU && selection.column == column) {
            // Clicking within the source column changes the start of the selected run.
            if (clicked.isFaceUp()) {
                selection = new Selection(SelectionType.TABLEAU, column, cardIndex, null);
                setStatus("Changed selection to " + clicked.shortName() + ".", true);
                refreshBoard();
            }
            return;
        }
        clickTableauDestination(column);
    }

    private void clickTableauDestination(int column) {
        if (selection == null) {
            setStatus("Select a card first.", false);
            return;
        }
        MoveResult result = switch (selection.type) {
            case WASTE -> game.moveWasteToTableau(column);
            case TABLEAU -> game.moveTableauToTableau(selection.column, selection.cardIndex, column);
            case FOUNDATION -> game.moveFoundationToTableau(selection.suit, column);
        };
        selection = null;
        report(result);
    }

    private void clickFoundation(Suit suit) {
        if (selection == null) {
            if (game.foundation(suit).isEmpty()) {
                setStatus("Select an Ace before clicking an empty foundation.", false);
                return;
            }
            selection = new Selection(SelectionType.FOUNDATION, -1, -1, suit);
            setStatus("Selected the " + suit.name().toLowerCase() + " foundation.", true);
            refreshBoard();
            return;
        }

        MoveResult result = switch (selection.type) {
            case WASTE -> game.moveWasteToFoundation();
            case TABLEAU -> game.moveTableauToFoundation(selection.column);
            case FOUNDATION -> MoveResult.failure("Choose a tableau destination for a foundation card.");
        };
        selection = null;
        report(result);
    }

    private void report(MoveResult result) {
        setStatus(result.message(), result.success());
        refreshBoard();
        if (game.isWon()) {
            JOptionPane.showMessageDialog(this, "Congratulations — you won!", "Victory",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void setStatus(String message, boolean positive) {
        status.setText(message);
        status.setForeground(positive ? Color.WHITE : new Color(255, 220, 130));
    }

    private boolean isSelected(SelectionType type, int column, int cardIndex, Suit suit) {
        return selection != null && selection.type == type
                && (type != SelectionType.TABLEAU
                    || (selection.column == column && selection.cardIndex == cardIndex))
                && (type != SelectionType.FOUNDATION || selection.suit == suit);
    }

    private void newGame() {
        int answer = JOptionPane.showConfirmDialog(this, "Start a new shuffled game?",
                "New Game", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            game.newGame();
            selection = null;
            setStatus("Started a new game.", true);
            refreshBoard();
        }
    }

    private void showInstructions() {
        String instructions = """
                Build tableau columns downward in alternating colors.
                Move Aces to foundations, then build each suit upward to King.

                • Click the stock to draw one card.
                • Click an empty stock to restock; restocks are unlimited.
                • Select a face-up card, then click its destination.
                • Selecting a tableau card also selects every card below it.
                • Only a King may move into an empty tableau column.
                """;
        JOptionPane.showMessageDialog(this, instructions, "How to Play",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private enum SelectionType { WASTE, TABLEAU, FOUNDATION }

    private record Selection(SelectionType type, int column, int cardIndex, Suit suit) {
    }

    /** A vector-drawn card; no external image assets or fonts are required. */
    private static final class CardView extends JComponent {
        private static final Color CARD_BACK = new Color(35, 66, 145);
        private final Card card;
        private final boolean selected;
        private final String backLabel;

        CardView(Card card, boolean selected, Runnable click) {
            this(card, selected, null, click);
        }

        private CardView(Card card, boolean selected, String backLabel, Runnable click) {
            this.card = card;
            this.selected = selected;
            this.backLabel = backLabel;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new ClickListener(click));
            setToolTipText(card == null || !card.isFaceUp() ? "Stock" : card.shortName());
        }

        static CardView faceDown(String count, Runnable click) {
            return new CardView(null, false, count, click);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(0, 0, 0, 55));
            g.fillRoundRect(3, 4, getWidth() - 4, getHeight() - 5, 12, 12);

            boolean faceUp = card != null && card.isFaceUp();
            g.setColor(faceUp ? new Color(250, 249, 245) : CARD_BACK);
            g.fillRoundRect(1, 1, getWidth() - 4, getHeight() - 5, 12, 12);
            g.setColor(selected ? new Color(255, 210, 50) : Color.DARK_GRAY);
            g.setStroke(new BasicStroke(selected ? 4f : 1.5f));
            g.drawRoundRect(1, 1, getWidth() - 4, getHeight() - 5, 12, 12);

            if (faceUp) {
                paintFace(g);
            } else {
                paintBack(g);
            }
            g.dispose();
        }

        private void paintFace(Graphics2D g) {
            Color ink = card.isRed() ? new Color(185, 26, 35) : new Color(26, 28, 32);
            g.setColor(ink);
            g.setFont(getFont().deriveFont(Font.BOLD, 20f));
            g.drawString(card.rank().label(), 9, 24);
            g.drawString(String.valueOf(card.suit().symbol()), 9, 46);
            g.setFont(getFont().deriveFont(Font.PLAIN, 44f));
            String suit = String.valueOf(card.suit().symbol());
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(suit, (getWidth() - metrics.stringWidth(suit)) / 2, 88);
        }

        private void paintBack(Graphics2D g) {
            g.setColor(new Color(115, 145, 220));
            g.setStroke(new BasicStroke(2f));
            for (int x = 9; x < getWidth() - 8; x += 10) {
                g.drawLine(x, 8, getWidth() - 8, getHeight() - x);
                g.drawLine(8, x, getWidth() - x, getHeight() - 8);
            }
            if (backLabel != null) {
                g.setColor(Color.WHITE);
                g.setFont(getFont().deriveFont(Font.BOLD, 15f));
                g.drawString(backLabel, 8, getHeight() - 10);
            }
        }
    }

    /** Draws an empty pile marker that can still act as a move destination. */
    private static final class EmptyPileView extends JComponent {
        private final String label;

        EmptyPileView(String label, Runnable click) {
            this.label = label;
            if (click != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new ClickListener(click));
            }
            setToolTipText(label);
        }

        static EmptyPileView stock(Runnable click) {
            return new EmptyPileView("Restock", click);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(FELT_DARK);
            g.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            g.setColor(new Color(255, 255, 255, 110));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    0, new float[]{7, 7}, 0));
            g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            g.setFont(getFont().deriveFont(Font.BOLD, label.length() > 2 ? 13f : 30f));
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(label, (getWidth() - metrics.stringWidth(label)) / 2,
                    (getHeight() + metrics.getAscent()) / 2 - 3);
            g.dispose();
        }
    }

    private static final class ClickListener extends MouseAdapter {
        private final Runnable action;

        ClickListener(Runnable action) {
            this.action = action;
        }

        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON1) {
                action.run();
            }
        }
    }
}
