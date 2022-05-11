
from vyper import v

import click
import uvicorn

@click.command(help="Run rest http server")
@click.option('--dev', default=False, help='Enable development mode (debug: true and reload: true).')
def rest(dev):
  click.echo("rest is running")

  uvicorn.run(
    "app.rest.api:api",
    host="0.0.0.0",
    port=v.get_int("port"),
    reload=dev,
    debug=dev,
    lifespan='on',
    log_config=None
  )
