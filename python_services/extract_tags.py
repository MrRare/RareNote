import sys
from sklearn.feature_extraction.text import ENGLISH_STOP_WORDS
from collections import Counter

def extract_tags(text):
    words = text.split()
    words = [word for word in words if word.lower() not in ENGLISH_STOP_WORDS]
    word_counts = Counter(words)
    tags = [word for word, count in word_counts.most_common(100)]
    return tags

if __name__ == "__main__":
    if len(sys.argv) > 1:
        text = sys.argv[1]
        try:
            tags = extract_tags(text)
            print(",".join(tags))
        except Exception as e:
            print("Error:", str(e))
    else:
        print("Error: No input text provided")