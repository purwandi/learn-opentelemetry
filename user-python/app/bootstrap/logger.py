from inspect import trace
from starlette.datastructures import State

import logging
import structlog

# from structlog.processors import JSONRenderer

structlog.configure(
    processors=[
        structlog.processors.add_log_level,
        structlog.processors.StackInfoRenderer(),
        structlog.dev.set_exc_info,
        structlog.processors.TimeStamper(fmt="iso"),
        structlog.processors.JSONRenderer(),
    ],
    wrapper_class=structlog.make_filtering_bound_logger(logging.NOTSET),
    context_class=dict,
    logger_factory=structlog.PrintLoggerFactory(),
    cache_logger_on_first_use=False,
)

logger: structlog.stdlib.BoundLogger = structlog.get_logger()

# def logWithContext(ctx: State):
#   if hasattr(ctx, 'trace_id') and ctx.trace_id != "":
#     logger.bind(
#       trace_id=format(ctx.trace_id, "032x"),
#     )

#   return logger
