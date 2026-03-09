import sys
import json
import os
import pickle
from typing import List, Dict, Any, Union, cast

# Robust imports with fallback for linting/IDE environments
try:
    import pandas as pd
except ImportError:
    pd = None

try:
    import nltk
    from nltk.corpus import stopwords
    from nltk.tokenize import word_tokenize
except ImportError:
    nltk = None
    stopwords = None
    word_tokenize = None

# Set paths
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DOMAIN_MODEL_PATH = os.path.join(BASE_DIR, "ai_models", "domain_classifier", "domain_classifier.pkl")
VECTORIZER_PATH = os.path.join(BASE_DIR, "ai_models", "domain_classifier", "tfidf_vectorizer.pkl")
MENTOR_DATA_PATH = os.path.join(BASE_DIR, "ai_models", "mentor_matching", "professors_dataset.csv")

# Initialize NLTK data silently
try:
    import io
    from contextlib import redirect_stdout
    with redirect_stdout(io.StringIO()):
        if nltk is not None:
            nltk.download('punkt', quiet=True)
            nltk.download('stopwords', quiet=True)
    if stopwords is not None:
        STOP_WORDS = set(stopwords.words('english'))
    else:
        STOP_WORDS = set()
except Exception:
    STOP_WORDS = set()

# Load models globally for efficiency
GLOBAL_MODEL: Any = None
GLOBAL_VECTORIZER: Any = None

try:
    if os.path.exists(DOMAIN_MODEL_PATH) and os.path.exists(VECTORIZER_PATH):
        with open(DOMAIN_MODEL_PATH, 'rb') as f:
            GLOBAL_MODEL = pickle.load(f)
        with open(VECTORIZER_PATH, 'rb') as f:
            GLOBAL_VECTORIZER = pickle.load(f)
except Exception:
    pass

def preprocess(text: str) -> str:
    """Standard preprocessing with tokenizer safety."""
    if not text or not isinstance(text, str):
        return ""
    
    text_lower = text.lower()
    
    # Tokenizer Safety
    try:
        if word_tokenize is not None:
            tokens = word_tokenize(text_lower)
        else:
            tokens = text_lower.split()
    except Exception:
        tokens = text_lower.split()
    
    # Filter non-alphabetic and stop words
    filtered_tokens = [str(w) for w in tokens if str(w).isalpha() and str(w) not in STOP_WORDS]
    return " ".join(filtered_tokens)

def classify_domain(description: str) -> List[str]:
    """Uses the pre-loaded global Domain Classification model."""
    if GLOBAL_MODEL is None or GLOBAL_VECTORIZER is None:
        return []
    
    clean_text = preprocess(description)
    X = GLOBAL_VECTORIZER.transform([clean_text])
    
    if hasattr(GLOBAL_MODEL, "predict_proba"):
        probs = GLOBAL_MODEL.predict_proba(X)
        classes = GLOBAL_MODEL.classes_
        
        # Casting to satisfy IDE/lint requirements for slicing
        first_row = cast(Any, probs)[0]
        # Sort indices by probability descending
        indices = first_row.argsort()
        top_3_indices = indices[-3:]
        
        # Explicit slicing to avoid lint warnings on list types
        top_3_list = list(top_3_indices)
        top_3_list.reverse()
        reversed_indices = top_3_list
        
        results = [str(classes[i]) for i in reversed_indices]
    else:
        results = [str(GLOBAL_MODEL.predict(X)[0])]
    
    return results

def suggest_mentors(project_domains: Union[str, List[str]]) -> List[Dict[str, Any]]:
    """Uses Cosine Similarity to rank mentors based on detected domains."""
    if not os.path.isfile(MENTOR_DATA_PATH) or pd is None or GLOBAL_VECTORIZER is None:
        return []
        
    if isinstance(project_domains, list):
        target_str = " ".join(project_domains)
    else:
        target_str = str(project_domains).replace(",", " ")
    
    df = pd.read_csv(MENTOR_DATA_PATH)
    
    # Vectorize project domains
    project_vector = GLOBAL_VECTORIZER.transform([preprocess(target_str)])
    
    recommendations = []
    from sklearn.metrics.pairwise import cosine_similarity
    
    for _, row in df.iterrows():
        # Availability check
        avail = str(row.get('availability', '')).strip().lower()
        if avail != 'available':
            continue
            
        expertise_str = str(row.get('expertise_domains', ''))
        mentor_vector = GLOBAL_VECTORIZER.transform([preprocess(expertise_str)])
        
        # Calculate Cosine Similarity
        similarity = float(cosine_similarity(project_vector, mentor_vector)[0][0])
        
        if similarity > 0:
            recommendations.append({
                "professorId": int(row.get('professor_id', 0)),
                "name": str(row.get('name', 'Unknown')),
                "score": round(similarity * 100, 2),
                "expertise": expertise_str
            })
    
    # Sort and return top 5
    recommendations.sort(key=lambda x: x['score'], reverse=True)
    return recommendations[:5]

def suggest_all(description: str) -> Dict[str, Any]:
    """Unified API returning both detected domains and suggested mentors."""
    detected_domains = classify_domain(description)
    recommended_mentors = suggest_mentors(detected_domains)
    return {
        "domains": detected_domains,
        "mentors": recommended_mentors
    }

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print(json.dumps({"error": "Missing arguments"}))
        sys.exit(1)
    
    command = sys.argv[1]
    input_data = sys.argv[2]
    
    try:
        if command == "classify":
            print(json.dumps(classify_domain(input_data)))
        elif command == "mentor":
            try:
                parsed = json.loads(input_data)
            except (json.JSONDecodeError, TypeError):
                parsed = input_data
            print(json.dumps(suggest_mentors(parsed)))
        elif command == "suggest_all":
            print(json.dumps(suggest_all(input_data)))
        else:
            print(json.dumps({"error": f"Unknown command: {command}"}))
    except Exception as err:
        print(json.dumps({"error": str(err)}))
