# Rules and Game Variation

This project implements classic Klondike with draw-one stock play and unlimited
restocks. The descriptions below are both player documentation and a behavioral
contract for the shared engine.

## Setup

A shuffled 52-card deck is dealt into seven tableau columns. Column one receives
one card, column two receives two, and so on through column seven. Only the last
card in each column is face up. The remaining 24 face-down cards form the stock.
The waste and four foundations begin empty.

## Tableau

Tableau cards build downward by exactly one rank and alternate color. A red Eight
may therefore be placed on a black Nine, but not on a red Nine or any Ten.

A face-up ordered run can move as a unit. After cards leave a tableau column, its
new top card is automatically turned face up. Only a King, either alone or at the
head of a valid run, may move to an empty tableau column.

## Stock and waste

Click or command `draw` to move exactly one card from the stock to the waste,
turning it face up. Only the waste's top card is playable.

When the stock is empty, the next draw action turns the waste over to recreate
the stock. The order is preserved so that the next pass presents cards in the
same sequence. Restocks are unlimited and do not incur a score penalty.

## Foundations

Each suit has its own foundation. A foundation begins with its Ace and builds
upward one rank at a time through King. Only a pile's top card can move. A top
foundation card may return to a legal tableau destination when needed.

## Winning

The game is won as soon as all 52 cards are in the four foundations. This project
does not currently implement scoring, hints, undo, timed mode, or automatic
foundation completion; these features can be layered on without changing the
base rules described above.
