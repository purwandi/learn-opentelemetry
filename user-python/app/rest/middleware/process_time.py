from fastapi import Request, Response
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.types import ASGIApp

import time

# https://stackoverflow.com/questions/71525132/how-to-write-a-custom-fastapi-middleware-class
class process_time(BaseHTTPMiddleware):

  def __init__(self, app: ASGIApp) -> None:
    super().__init__(app)

  async def dispatch(self, request: Request, call_next) -> Response:
    content_type = request.headers.get('Content-Type')
    # print(content_type)

    start_time = time.time()
    response = await call_next(request)

    process_time = time.time() - start_time
    response.headers["X-Process-Time"] = str(process_time)

    return response
