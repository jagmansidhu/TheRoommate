# DaRoomate Deployment Guide

This directory contains deployment configurations and documentation for deploying DaRoomate to various platforms.

## Quick Start - Railway Deployment

Railway is a platform-as-a-service that makes deploying containerized apps simple.

### Prerequisites

- Railway account ([railway.app](https://railway.app))
- GitHub repository connected to Railway
- All environment variables ready (see below)

### Step 1: Create Railway Project

1. Go to [Railway Dashboard](https://railway.app/dashboard)
2. Click **New Project** → **Deploy from GitHub repo**
3. Select your DaRoomate repository

### Step 2: Create Backend Service

1. In your Railway project, click **Add Service** → **GitHub Repo**
2. Select the `backend` folder as the root directory
3. Railway will automatically detect the Dockerfile

### Step 3: Add PostgreSQL Database

Option A: **Railway PostgreSQL (Recommended for simplicity)**

1. Click **Add Service** → **Database** → **PostgreSQL**
2. Railway will auto-populate the database connection variables

Option B: **External Database (AWS RDS, etc.)**

1. Skip this step
2. Manually configure database environment variables below

### Step 4: Configure Backend Environment Variables

In the backend service, go to **Variables** and add:

```
# Database (if using external DB, otherwise Railway auto-populates these)
POSTGRESQL_HOST=<your-db-host>
POSTGRESQL_PORT=5432
POSTGRESQL_DATABASE=<your-db-name>
POSTGRESQL_USERNAME=<your-db-user>
POSTGRESQL_PASSWORD=<your-db-password>

# Application
JWT_KEY=<your-jwt-secret-min-32-chars>
ACTIVE_PROFILE=prod
CONTAINER_PORT=${{PORT}}

# Email (SMTP)
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_ID=<your-email>
EMAIL_PASSWORD=<your-app-password>
```

> **Note:** `${{PORT}}` is a Railway variable reference that injects the assigned port.

### Step 5: Create Frontend Service

1. Click **Add Service** → **GitHub Repo**
2. Select the `frontend` folder as the root directory
3. Add build variable:

```
REACT_APP_BASE_API_URL=https://<your-backend-service>.railway.app
```

> **Important:** This must be set as a build-time variable since React embeds it during `npm run build`.

### Step 6: Generate Domains

1. For each service, go to **Settings** → **Networking**
2. Click **Generate Domain** or add a custom domain

### Step 7: Deploy

Railway automatically deploys when you push to your repository. You can also trigger manual deploys from the dashboard.

---

## Environment Variables Reference

### Backend Variables

| Variable              | Required | Description                    | Example                              |
| --------------------- | -------- | ------------------------------ | ------------------------------------ |
| `POSTGRESQL_HOST`     | Yes      | Database hostname              | `postgres.railway.internal`          |
| `POSTGRESQL_PORT`     | Yes      | Database port                  | `5432`                               |
| `POSTGRESQL_DATABASE` | Yes      | Database name                  | `daroomate`                          |
| `POSTGRESQL_USERNAME` | Yes      | Database user                  | `postgres`                           |
| `POSTGRESQL_PASSWORD` | Yes      | Database password              | `****`                               |
| `JWT_KEY`             | Yes      | JWT signing secret (32+ chars) | `your-super-secret-key-here-32chars` |
| `ACTIVE_PROFILE`      | No       | Spring profile                 | `prod`                               |
| `CONTAINER_PORT`      | No       | Server port (default: 8085)    | `8085`                               |
| `EMAIL_HOST`          | Yes      | SMTP host                      | `smtp.gmail.com`                     |
| `EMAIL_PORT`          | Yes      | SMTP port                      | `587`                                |
| `EMAIL_ID`            | Yes      | SMTP username                  | `app@example.com`                    |
| `EMAIL_PASSWORD`      | Yes      | SMTP password/app password     | `****`                               |

### Frontend Variables

| Variable                 | Required         | Description     | Example                       |
| ------------------------ | ---------------- | --------------- | ----------------------------- |
| `REACT_APP_BASE_API_URL` | Yes (build-time) | Backend API URL | `https://backend.railway.app` |

---

## Local Testing with Docker Compose

You can test the production configuration locally:

```bash
cd deploy

# Copy and fill in environment variables
cp env.example .env
# Edit .env with your values

# Build and run
docker compose -f docker-compose.production.yml up --build
```

---

## Future: AWS Migration

The Docker-based setup is designed to be portable. Here are your AWS options:

### Option 1: AWS Elastic Beanstalk (Simplest)

1. Install EB CLI: `pip install awsebcli`
2. Initialize: `eb init`
3. Use the Procfile for deployment
4. Create environment: `eb create production`

### Option 2: AWS ECS (Fargate)

1. Push Docker images to ECR
2. Create ECS task definitions using the Dockerfiles
3. Set up an Application Load Balancer
4. Configure service and desired count

### Option 3: AWS ECS with Docker Compose

AWS ECS now supports Docker Compose directly:

```bash
# Install ECS CLI plugin
# Configure AWS credentials

cd deploy
docker compose -f docker-compose.production.yml up  # Local test
ecs-cli compose up  # Deploy to ECS
```

### AWS-Specific Considerations

1. **RDS PostgreSQL**: Use RDS instead of Railway PostgreSQL
2. **Secrets Manager**: Store sensitive env vars in AWS Secrets Manager
3. **CloudFront**: Add CloudFront CDN in front of the frontend for caching
4. **Route 53**: Use Route 53 for DNS management
5. **ACM**: Free SSL certificates via AWS Certificate Manager

---

## Troubleshooting

### Common Issues

**Backend won't start:**

- Check database connection variables
- Verify JWT_KEY is at least 32 characters
- Check Railway build logs for errors

**Frontend shows blank page:**

- Verify REACT_APP_BASE_API_URL was set at build time
- Check browser console for API errors
- Ensure backend CORS allows frontend domain

**Database connection refused:**

- If using Railway DB, ensure you're using internal hostname
- Check that database service is running
- Verify credentials are correct

### Viewing Logs

Railway:

```bash
# Install Railway CLI
npm install -g @railway/cli

# Login and view logs
railway login
railway logs
```

---

## Files in This Directory

- `docker-compose.production.yml` - Production compose file for local testing
- `env.example` - Template for environment variables
- `README.md` - This file
