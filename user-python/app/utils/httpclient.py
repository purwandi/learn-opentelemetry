from opentelemetry.instrumentation.aiohttp_client import create_trace_config
from typing import Optional
from socket import AF_INET

import aiohttp
import yarl


def strip_query_params(url: yarl.URL) -> str:
    return str(url.with_query(None))


class HttpClient:
    httpClient: Optional[aiohttp.ClientSession] = None

    @classmethod
    def instance(cls) -> aiohttp.ClientSession:
        if cls.httpClient is None:
            timeout = aiohttp.ClientTimeout(total=60)
            connector = aiohttp.TCPConnector(
                limit=100, family=AF_INET, limit_per_host=100
            )
            cls.httpClient = aiohttp.ClientSession(
                timeout=timeout,
                connector=connector,
                trace_configs=[create_trace_config(url_filter=strip_query_params)],
            )

        return cls.httpClient

    @classmethod
    async def close(cls):
        if cls.httpClient:
            await cls.httpClient.close()
            cls.httpClient = None
