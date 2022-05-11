from .rest import rest
from .migration import migrate_up, migrate_make

import click

@click.group()
def cli():
  pass

cli.add_command(rest)
cli.add_command(migrate_up)
cli.add_command(migrate_make)
