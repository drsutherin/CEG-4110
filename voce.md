# Speech Recognition in BAAC
BAAC supports speech recognition for making moves while playing checkers games!  Once it's your turn, you can speak your moves to the computer, and it will make them for you!

### Making Moves by Speaking
In order to make a move by speaking, you'll need to say it in a manner that BAAC will recognize.  You'll need to say "Computer" to let BAAC know you're about to make a move, followed by the location of the piece you want to move, then the word "move", and finally the location you'd like to move the piece to.  BAAC uses the NATO phonetic alphabet for all column labels to ensure that it can tell the difference between similar sounding letters ('bee', 'cee', 'dee', 'ee', 'gee')

### Example Spoken Moves
* "Computer alpha one move bravo two" moves the piece at position A1 to B2
* "Computer echo three move delta four" moves the piece at position E3 to D4

### NATO Phonetic Alphabet
Below is a list of letters and their corresponding phonetic representation:
* A: Alpha
* B: Bravo
* C: Charlie
* D: Delta
* E: Echo
* F: Foxtrot
* G: Golf
* H: Hotel

### Voce
Speech recognition is performed with open source software called Voce which can be found [on SourceForge](http://voce.sourceforge.net/).  All credit for Voce development goes to Tyler Streeter
