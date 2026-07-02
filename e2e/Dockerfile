# Playwright Python E2E — 基于官方镜像（已含 Chromium 与系统依赖）
FROM mcr.microsoft.com/playwright/python:v1.52.0-jammy

WORKDIR /app

# uv 管理 Python 依赖（pip 安装，避免 ghcr.io 拉取失败）
RUN pip install --no-cache-dir uv

ENV UV_COMPILE_BYTECODE=1 \
    UV_LINK_MODE=copy

COPY pyproject.toml uv.lock ./
RUN uv sync --frozen --no-dev

COPY conftest.py ./
COPY tests ./tests

# 默认连 compose 网络内的 frontend / backend
ENV BASE_URL=http://host.docker.internal:5173 \
    API_URL=http://host.docker.internal:8080

CMD ["uv", "run", "pytest", "tests/", "-v"]
