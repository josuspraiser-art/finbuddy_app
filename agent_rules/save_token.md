# Token Optimization Guidelines

To optimize token usage and context size for the Antigravity agent, follow these rules:

## 1. Response Conciseness
- **Short Explanations:** Keep explanations brief and technical. Skip boilerplate summaries, pleasantries, or chatty introductions/outros.
- **No Artifact Re-summarization:** When creating or updating an artifact, do not write a detailed summary in the chat response. Point directly to the artifact and only list critical decisions or questions.

## 2. File Operations
- **Targeted Edits:** Always prefer using `replace_file_content` or `multi_replace_file_content` with minimal start/end lines to edit specific chunks. Avoid overwriting or reading entire files unless strictly necessary.
- **Code Diffs:** Display only relevant code changes or diff blocks rather than printing the entire file in the response.

## 3. Tool Execution
- **Minimal Output:** When verifying or testing code, output only the success status or critical error logs instead of full build logs.
