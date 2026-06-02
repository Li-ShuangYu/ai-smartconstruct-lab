from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from routers import health
from routers import orchestration
from routers import chat
from routers import ide

app = FastAPI(
    title="AI Engine",
    description="AI SmartConstruct Lab - AI Engine Microservice",
    version="1.0.0"
)


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(
    request: Request, exc: RequestValidationError
) -> JSONResponse:
    """Format Pydantic/FastAPI validation errors consistently (Requirement 9.5).

    Returns 422 with error_code and error_message fields.
    """
    errors = exc.errors()
    messages = []
    for error in errors:
        loc = " -> ".join(str(part) for part in error.get("loc", []))
        msg = error.get("msg", "Validation error")
        messages.append(f"{loc}: {msg}")

    return JSONResponse(
        status_code=422,
        content={
            "error_code": "VALIDATION_ERROR",
            "error_message": "; ".join(messages),
            "details": errors,
        },
    )


app.include_router(health.router, prefix="/api", tags=["health"])
app.include_router(orchestration.router, tags=["orchestration"])
app.include_router(chat.router, tags=["chat"])
app.include_router(ide.router, tags=["ide"])


@app.get("/")
async def root():
    return {"message": "AI Engine is running", "version": "1.0.0"}
