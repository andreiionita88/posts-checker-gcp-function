# Post Checker Function
GCP Cloud Function that listens to Pub/Sub messages and parses them to check for words that need to be replaced / censored in the posts database (datastore)

## Deployment
To deploy the function run:
```
gcloud functions deploy posts-checker --trigger-topic=posts --runtime=java17 --entry-point=com.example.PostCheckerFunction
```