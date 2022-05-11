#!/usr/bin/env python
# -*- coding: utf-8 -*-
import os
import sys
import time
from typing import List, Any
import asyncpg

migration_template = """#!/usr/bin/env python
# -*- coding: utf-8 -*-
import asyncpg


async def up(db: asyncpg.Connection):
  await db.execute('''

  ''')


async def down(db: asyncpg.Connection):
  await db.execute('''

  ''')
"""


async def ensure_migration_table(db: asyncpg.Connection) -> bool:
    """Create migration table if not exists"""
    await db.execute(
        """CREATE TABLE IF NOT EXISTS migration (
    id SERIAL PRIMARY KEY,
    file_name VARCHAR(200) NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT NOW()
  )"""
    )
    return True


async def select_migrations(db: asyncpg.Connection) -> List[asyncpg.Record]:
    """Select all migrations from migration table"""
    return await db.fetch("""SELECT * FROM migration""")


async def select_migration_file_name(
    db: asyncpg.Connection, file_name
) -> asyncpg.Record:
    """Select migration by file name"""
    return await db.fetchrow(
        """SELECT * FROM migration where file_name = $1""", file_name
    )


async def insert_migration(db: asyncpg.Connection, file_name):
    """Insert new migration"""
    await db.execute("""INSERT INTO migration (file_name) VALUES ($1)""", file_name)


async def delete_migration(db: asyncpg.Connection, file_name):
    """Delete migration"""
    await db.execute("""DELETE FROM migration where file_name = $1""", file_name)


def import_module(directory: str, fname: str) -> Any:
    module_name = ".".join(fname.split(".")[:-1])
    m = __import__(f"{directory}.{module_name}")
    return getattr(m, module_name)


def print_error(msg: str):
    print(f"--ERROR-- {msg}")


def print_info(msg: str):
    print(f"{msg}")


def add_migration(name: str, directory: str):
    """Add migration file"""
    dt = int(time.time())
    name = "_".join(name.strip().split(" "))
    fpath = os.path.join(directory, f"{dt}_{name}.py")
    if os.path.exists(fpath):
        raise RuntimeError(f"Path {fpath} already exists")
    with open(fpath, "w+") as f:
        f.write(migration_template)


async def migrate_database(db: asyncpg.Connection, fname: str, directory: str):
    """Migrate database to latest version"""
    await ensure_migration_table(db)
    if fname == "all":
        await migrate_all(db, directory)
    else:
        await migrate_one(db, fname)


async def migrate_all(db: asyncpg.Connection, directory: str):
    """Migrate all missing migrations"""
    rows = await select_migrations(db)
    for fname in os.listdir(directory):
        if fname.endswith(".py") and not fname.startswith("__"):
            found = False
            for row in rows:
                r = dict(list(row.items()))
                if r["file_name"] == fname:
                    found = True
                    break
            if not found:
                module = import_module(directory=directory, fname=fname)
                await module.up(db)
                await insert_migration(db, file_name=fname)
                print_info(f"Applying {fname}")
            else:
                print_info(f"Skipping {fname}")
        pass
    pass


async def migrate_one(db: asyncpg.Connection, fpath: str):
    """Migrate one file"""
    if not os.path.exists(fpath):
        print_error(f"Migrate file `{fpath}` not exists")
        return
    fname = fpath.split("/")[-1]
    # check if migration exists
    result = await select_migration_file_name(db, file_name=fname)
    if result:
        print_error(f"Migration `{fpath}` found in migration table")
        return
    # migrate
    directory = "/".join(fpath.split("/")[:-1])
    module = import_module(directory=directory, fname=fname)
    await module.up(db)
    await insert_migration(db, file_name=fname)
    print_info(f"Migrated file `{fpath}`")


async def rollback_migration(db: asyncpg.Connection, fpath: str):
    """Rollback migration"""
    if not os.path.exists(fpath):
        print_error(f"Rollback file `{fpath}` not exists")
        return
    fname = fpath.split("/")[-1]
    # check if migration exists
    await ensure_migration_table(db)
    result = await select_migration_file_name(db, file_name=fname)
    if not result:
        print_error(f"Migration `{fpath}` not found in migration table")
        return
    directory = "/".join(fpath.split("/")[:-1])
    module = import_module(directory=directory, fname=fname)
    await module.down(db)
    await delete_migration(db, file_name=fname)
    print_info(f"Rollback migration `{fname}`")


async def list_migrations(db: asyncpg.Connection, directory: str):
    await ensure_migration_table(db)
    rows = await select_migrations(db)
    todo = []
    done = []
    for fname in sorted(os.listdir(directory)):
        if fname.endswith(".py") and not fname.startswith("__"):
            for row in rows:
                r = dict(list(row.items()))
                if r["file_name"] == fname:
                    done.append(fname)
                    break
            else:
                todo.append(fname)

    [print_info(f"DONE {name}") for name in done]
    [print(f"TODO {name}") for name in todo]
