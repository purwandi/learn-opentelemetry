from typing import AnyStr

import asyncpg


async def connection(url: AnyStr) -> asyncpg.Connection:
    return await asyncpg.connect(dsn=url)


async def pool(url: str) -> asyncpg.Pool:
    return await asyncpg.create_pool(dsn=url, min_size=10, max_size=20)  # type: ignore
