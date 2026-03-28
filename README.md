# Crypto Market Monitor
Java desktop application for monitoring real-time cryptocurrency market data through interactive charts and live API integration.

## Overview
This project is a desktop application built with **JavaFX** and **Spring** that allows users to monitor the performance of major cryptocurrencies using real-time market data. The application provides dynamic visualizations across different time intervals and implements a custom **web caching strategy** to reduce redundant API calls and improve performance.

## Documentation
A detailed project report (in Italian) is available in the `docs/` folder.

## Features
- Real-time cryptocurrency data retrieval through external APIs
- Interactive time-series charts for different time intervals
- Desktop graphical interface built with JavaFX
- Modular architecture with separation between UI, business logic, and data access
- Custom web caching policy to optimize repeated requests and reduce unnecessary network traffic

## Technologies Used
- Java
- JavaFX
- Spring
- REST API integration

## Architecture
The application is structured to separate:
- **Presentation layer** for the graphical user interface
- **Business logic layer** for data processing
- **Data access layer** for API communication and caching

## Caching Strategy
The application includes a custom web caching mechanism designed to:
- avoid repeated requests for recently fetched data
- reduce response latency
- improve efficiency when users inspect the same cryptocurrency over short time intervals
