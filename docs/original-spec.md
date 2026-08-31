# Original Game Specification

This is the original prompt that kicked off the WordWang implementation, kept here for future reference.

---

Plan the implementation for the game. The idea of the game is as follows:
* The system will randomly select an 8 letter word from a dictionary, and scramble the word
* The scrambled word will be shown to the player(s) along with a 30-second countdown timer
* The aim of the game is to find as many words as possible using the letters from the randomly selected word, as well as trying to find the full 8-letter word.
* The system will validate words against the dictionary. When the player enters a valid word, respond accordingly e.g. "+3 points!". If not valid, respond with "<Word> is not a word!". If the word is less then three letters, respond with "<Word> is too short!"
* Display the scrambled word using a font that looks like tiles or the keys on a keyboard
* Users can click on the tiles themselves or type using the keyboard, but they can only use the letters from the scrambled word, and they cannot use letters more than one (the input control should enforce this).
* Players will get more points for longer words:
** Less than 3 letters - no points
** 3 letters - 1 point
** 4 letters - 3 points
** 5 letters - 7 points
** 6 letters - 10 points
** 7 letters - 15 points
** 8 letters - 20 points
* The home page will contain a "New Game" and "Join Game" button, as well as a "How to Play" link, with instructions.
* After clicking "New Game", the original player (organiser) will be prompted to enter their name, then there will be two further options: (1) "Invite" and (2) "Start Game"
* "Invite" will create a link that can be shared, for example, via WhatsApp. When they click on the link, they will the name of the organiser and other players who have joined (updated in real time) and there will be a message, e.g. "Waiting for the game to start..."
* New game creates a unique 5-digit game id. This will be used in the invite link, or players without the link can click on Join Game and enter the code to join the game.
* Once invited players have joined, the organiser can click the "Start Game" button (note that only the organiser sees this button, invited players have to wait for the organiser to start the game)
* The game can also be played by a single player against the clock (i.e. there is no need to invite other players)
* Each player will see the names and scores of the other players in real-time. The players will be listed in descending points order
* The player with the highest score when the countdown elapses wins. When the game ends, the system will reveal the 8-letter word and announce the winner.
* Maintain a High Score table with: Player's name, Score, Date
* Use a cookie to pre-populate the name field when a player returns to the game on future occasions.

---

Note: the round timer specified above (30 seconds) was later changed, first to 3 minutes and then to 2 minutes — see git history / `GameService.ROUND_DURATION`. The organiser-only "Quit Game" early-end button and the tile-input spacing tweaks were also added after this original spec.
