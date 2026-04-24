from sentence_transformers import SentenceTransformer

print("开始下载并缓存向量模型权重...")
# 这会在有网时提前把本地 RAG 需要的轻量级向量模型下载到 ~/.cache/huggingface 目录下
model = SentenceTransformer('all-MiniLM-L6-v2')
print("缓存成功！断网后可安全调用。")