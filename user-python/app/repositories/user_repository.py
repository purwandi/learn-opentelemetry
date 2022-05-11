from typing import Union
from pypika import Table, Query, Parameter
from ..models.user import User

import asyncpg


async def create(db: asyncpg.Pool, user: User):
    table = Table("users")
    query = (
        Query.into(table)
        .columns("name", "email", "address")
        .insert(Parameter("$1"), Parameter("$2"), Parameter("$3"))
    )

    conn = await db.acquire()
    try:
        await conn.execute(query.get_sql(), user.name, user.email, user.address)
    finally:
        await db.release(conn)


async def update(db: asyncpg.Pool, user: User):
    table = Table("users")
    query = (
        Query.update(table)
        .set(table.name, Parameter("$1"))
        .set(table.email, Parameter("$2"))
        .set(table.address, Parameter("$3"))
        .where(table.id == Parameter("$4"))
    )

    conn = await db.acquire()
    try:
        await conn.execute(
            query.get_sql(), user.name, user.email, user.address, user.id
        )
    finally:
        await db.release(conn)


async def find(db: asyncpg.Pool, id: int) -> Union[User, None]:
    table = Table("users")
    query = Query.from_(table).select("*").where(table.id == Parameter("$1"))

    conn = await db.acquire()
    try:
        user = await conn.fetchrow(query.get_sql(), id)
        if user is not None:
            return User.parse_obj(dict(user))
    finally:
        await db.release(conn)


async def delete(db: asyncpg.Pool, id: int):
    table = Table("users")
    query = Query.from_(table).delete().where(table.id == Parameter("$1"))

    conn = await db.acquire()
    try:
        await conn.execute(query.get_sql(), id)
    finally:
        await db.release(conn)
