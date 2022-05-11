#!/usr/bin/env python
# -*- coding: utf-8 -*-
import asyncpg


async def up(db: asyncpg.Connection):
  await db.execute('''
CREATE TABLE users(
  id SERIAL PRIMARY KEY,
  name VARCHAR(100),
  email VARCHAR(100) UNIQUE NOT NULL,
  address TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
  ''')


async def down(db: asyncpg.Connection):
  await db.execute('''
DROP TABLE users;
  ''')
