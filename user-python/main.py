from app.cmd import cli
from vyper import v

v.add_config_path('.')
v.set_config_file('config.yaml')
v.set_config_type('yaml')
v.read_in_config()

if __name__ == '__main__':
  cli()
