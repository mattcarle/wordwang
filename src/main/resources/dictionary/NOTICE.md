# Word list provenance

`enable1.txt` is the ENABLE word list ("Enhanced North American Benchmark
Lexicon"), compiled by Alan Beale and released to the public domain for use
in word-game software. It is the de facto standard free word list for
Scrabble-style games (~172,800 words, one per line, lowercase).

Sourced from https://github.com/dolph/dictionary (mirror of the original
public-domain list).

`eight_letter_common.txt` is the subset of ENABLE1's 8-letter words that also
appear in Peter Norvig's word-frequency list derived from Google's Trillion
Word Corpus (https://norvig.com/ngrams/count_1w.txt), used to pick the daily
and per-round target word. Filtering against real-world usage frequency keeps
obscure/technical entries (e.g. AASVOGEL, ABFARADS) out of the pool players
have to actually find, while `enable1.txt` still governs which typed guesses
score points.
