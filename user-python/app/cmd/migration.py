from ..bootstrap import database
from ..utils.coroutine import coro
from ..utils.migration import add_migration, migrate_database
from vyper import v

import asyncpg
import click
import os

@click.command(name="migrate:up", help="Run database up")
@coro
async def migrate_up():
  click.echo("Running database migration")
  db: asyncpg.Connection = await database.connection(v.get("database.connection.write"))
  await migrate_database(db, "all", v.get("database.migration.path"))
  await db.close()


@click.command(name="migrate:make", help="Create database migrations")
@click.argument('name')
def migrate_make(name):
  path = v.get("database.migration.path")

  if not os.path.exists(path):
    os.makedirs(path)

  add_migration(name, path)
  click.echo("migration is created")
