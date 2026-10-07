# SliceQuote

> Backend service for a 3D-print cost estimation site. Pick a material, upload an STL, get a priced estimate back.



**Live:** https://slicequote.abdulkaderbaghaffar.com 
---


## Stack

- **Java 21 + Spring Boot** 
- **Postgres 16**
- **JWT** 
- **PrusaSlicer CLI**
- **Docker** 
- **GitHub Actions**
- Deployed on **Home Server** 

## Architecture

A layer only talks to the layer below it.

| Layer | Package | Job |
|---|---|---|
| Controller | `controller/` | HTTP only: JSON in, JSON + status out |
| Service | `service/` | Business rules (pricing, ownership, lifecycle) |
| Worker | `worker/` | Async slice job, status transitions |
| Slicer | `slicer/` | The **only** class touching `ProcessBuilder` |
| Repository | `repository/` | DB access (Spring generates the impls) |
| Entity | `data/` | Stored rows (JPA lives in `data`, not `entity`) |
| DTO | `dto/` | Request/response shapes at the API boundary |
| Config | `config/` | Beans, security rules, `@ControllerAdvice` |
