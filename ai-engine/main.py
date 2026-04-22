from fastapi import FastAPI
from routers import health

app = FastAPI(
    title="AI Engine",
    description="AI SmartConstruct Lab - AI Engine Microservice",
    version="1.0.0"
)

app.include_router(health.router, prefix="/api", tags=["health"])


@app.get("/")
async def root():
    return {"message": "AI Engine is running", "version": "1.0.0"}
