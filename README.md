# Google Scholar API – SerpApi Integration

## 📖 Project Purpose
The goal of this project is to **explore and document the use of the Google Scholar API through SerpApi**. It includes a technical report summarizing essential API details and a functional GitHub repository with clear documentation for collaboration.

---

## 🚀 Key Functionalities
- **SerpApi Setup**
    - Created a SerpApi account under the free plan.
    - Obtained an API key to authenticate requests.

- **Technical Documentation**
    - Endpoints (URLs used to query the API).
    - Authentication methods.
    - Query parameters for filtering and customization.
    - Response formats (JSON).
    - Usage limits under the free plan.
    - Code examples in multiple programming languages.

- **GitHub Repository**
    - Contains this README (project overview + full technical report).
    - Configured access for the **Digital NAO team**.

---

## 📌 Project Relevance
- Facilitates **academic research automation** by retrieving structured results from Google Scholar.
- Provides a **developer-friendly guide** to using the SerpApi Google Scholar integration.
- Ensures **reproducibility** with code examples and clear documentation.

---

## 🛠️ Technologies Used
- **SerpApi – Google Scholar API**
- **JSON** (response format)
- **Python, Node.js, Java** (example integrations)
- **GitHub** (project hosting and collaboration)

---

# 📑 Technical Report – Google Scholar API (SerpApi)

## 1. Endpoints
Base URL:
```
https://serpapi.com/search
```

- **Google Scholar Author Search**
  ```
  https://serpapi.com/search?engine=google_scholar_author
  ```

- **Google Scholar Search (Articles/Keywords)**
  ```
  https://serpapi.com/search?engine=google_scholar
  ```

---

## 2. Authentication Methods
- Sign up at [https://serpapi.com](https://serpapi.com).
- Retrieve API key from your dashboard.
- Add it to each request:

```
https://serpapi.com/search?engine=google_scholar&q=machine+learning&api_key=YOUR_API_KEY
```

---

## 3. Query Parameters

| Parameter | Description | Example |
|-----------|-------------|---------|
| `q` | Search query string | `q=quantum+computing` |
| `engine` | Specifies the engine | `engine=google_scholar` |
| `api_key` | Authentication key | `api_key=xxxx` |
| `hl` | Language of results | `hl=en` |
| `start` | Pagination offset | `start=10` |
| `author_id` | Search by author ID | `author_id=AbC123XYZ` |

---

## 4. Response Formats
Responses are in **JSON**.

Example:
```json
{
  "organic_results": [
    {
      "title": "Deep Learning",
      "authors": "Y. LeCun, Y. Bengio, G. Hinton",
      "publication_date": "2015",
      "link": "https://scholar.google.com/scholar?cluster=123456",
      "cited_by": 20000
    }
  ]
}
```

---

## 5. Usage Limits
- **Free Plan**: 100 searches/month.
- **Rate Limit**: 1 request/second.
- Higher usage requires a paid plan.

---

## 6. Code Examples

### Python
```python
import requests

params = {
    "engine": "google_scholar",
    "q": "artificial intelligence",
    "api_key": "YOUR_API_KEY"
}

response = requests.get("https://serpapi.com/search", params=params)
data = response.json()

for result in data.get("organic_results", []):
    print(result["title"], "-", result["authors"])
```

### JavaScript (Node.js)
```javascript
const axios = require("axios");

async function scholarSearch() {
  const response = await axios.get("https://serpapi.com/search", {
    params: {
      engine: "google_scholar",
      q: "data science",
      api_key: "YOUR_API_KEY"
    }
  });
  console.log(response.data.organic_results);
}

scholarSearch();
```

### Java (HttpClient)
```java
import java.net.http.*;
import java.net.URI;

public class ScholarSearch {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://serpapi.com/search?engine=google_scholar&q=machine+learning&api_key=YOUR_API_KEY"))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }
}
```

---

## 7. Conclusion
The **Google Scholar API via SerpApi** provides developers with a structured way to query authors, articles, and citation data.

By following this documentation, you can:
- Retrieve research results and author data.
- Integrate results into applications or databases.
- Handle authentication, query customization, and usage limits effectively.  
