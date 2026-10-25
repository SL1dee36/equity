# Equity — Smart Budget & Wealth Calculator

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture%20%2F%20MVI-00E676.svg)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

> **"Equity" represents the ultimate financial goal: building net worth through perfect financial balance and calculated asset growth.**

Equity is a production-ready native Android personal finance application built with Kotlin, Jetpack Compose, Room Database, and Koin Dependency Injection following strict Clean Architecture and Unidirectional Data Flow (UDF) principles.

---

## 🌟 Key Highlights

### 1. The Balance (Income & Expense Ledger)
- **High-Velocity Tracking**: Add, edit, and delete transactions with categorizations, recurring tags, and timestamp indexing.
- **Dynamic Net Balance**: Real-time calculation of net worth ($$\text{Current Balance} = \sum \text{Income} - \sum \text{Expense}$$) emitting via Kotlin Flow directly from Room DB.
- **Instant Filtering & Search**: Instant real-time filtering by transaction type (All, Income, Expense) and category/note search.

### 2. The Equity Formula (Smart Savings Engine)
The app's killer feature transforms raw budgeting into an active wealth-acceleration algorithm:
- **Discretionary Income Analysis**: Automatically separates recurring/essential commitments (rent, utilities, groceries) from discretionary cash flow.
- **"Must Save" Ratio ($R_{save}$)**: Computes the precise percentage of income required to be locked away to hit a target net worth by a target date.
- **"Safe to Spend" Allowance ($S_{safe}$)**: Computes the exact daily and monthly safe-to-spend allowance that ensures your savings trajectory remains untouched.
- **Dynamic Incoming Auto-Lock**: For every new incoming paycheck or deposit, calculates exactly how much money should be instantly sequestered into your Equity vault.
- **Deficit & Feasibility Advisory**: Intelligently classifies goals into **Optimal**, **Healthy**, **Aggressive**, **At Risk**, or **Unfeasible**, suggesting automatic timeline extensions when saving demands exceed discretionary capacity.

### 3. Dashboard & Analytics
- **Hero Equity Card**: High-contrast Obsidian card with real-time net balance, percentage savings rate, and monthly cash flow badges.
- **Custom Donut Pie Chart**: Canvas-drawn expense distribution segmented by category with animated sweep transitions and interactive legends.
- **Comparative Bar Chart**: Side-by-side monthly income vs. expense cash flow trends.
- **Progressive Goal Vault**: Live milestone tracking with progress meters and countdown horizons.

---

## 🎨 UI/UX & Design System

The application features a bespoke **Obsidian & Electric Mint** FinTech aesthetic:
- **Primary Background**: Deep Obsidian (`#121212`, `#181A1B`, `#1F2224`)
- **Accent**: Electric Mint / Emerald Wealth (`#00E676`, `#00B359`, `#69F0AE`) symbolizing net worth growth
- **Secondary**: Muted Silver & Platinum (`#ECEFF1`, `#B0BEC5`, `#78909C`)
- **Feedback**: Crisp Expense Crimson (`#FF5252`) and Golden Amber (`#FFB300`)
- **Material 3**: Edge-to-edge system bars, animated bottom navigation pills, and fluid modal bottom sheets.

---

## 📐 Clean Architecture

Equity enforces strict separation of concerns across three distinct layers:

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                     │
│  Compose Screens • ViewModels (StateFlow) • Design Tokens   │
└──────────────────────────────┬──────────────────────────────┘
                               │ uses
┌──────────────────────────────▼──────────────────────────────┐
│                        Domain Layer                         │
│  UseCases • Models • EquityFormulaCalculator (Pure Kotlin)  │
└──────────────────────────────▲──────────────────────────────┘
                               │ implements
┌──────────────────────────────┴──────────────────────────────┐
│                         Data Layer                          │
│  Room DB (DAOs, Entities) • Repositories • Offline Vault    │
└─────────────────────────────────────────────────────────────┘
```

### Module Structure
```
app/src/main/java/com/equity/app/
├── core/
│   ├── theme/          # Color tokens, Typography, Theme definition
│   └── util/           # Currency, date, and percentage formatters
├── data/
│   ├── local/
│   │   ├── dao/        # Room TransactionDao & SavingsGoalDao
│   │   ├── entity/     # TransactionEntity & SavingsGoalEntity
│   │   ├── EquityDatabase.kt
│   │   └── EquityDatabaseSeeder.kt
│   └── repository/     # TransactionRepositoryImpl & SavingsGoalRepositoryImpl
├── domain/
│   ├── calculator/     # EquityFormulaCalculator math engine
│   ├── model/          # Clean domain models & enums
│   ├── repository/     # Abstract repository contracts
│   └── usecase/        # Granular, single-responsibility use cases
├── di/
│   └── AppModule.kt    # Koin dependency injection configuration
└── presentation/
    ├── calculator/     # Smart Savings Calculator Screen & ViewModel
    ├── components/     # Custom canvas charts, stat cards, bottom bar
    ├── dashboard/      # Executive Dashboard Screen & ViewModel
    ├── navigation/     # Jetpack Compose Navigation Graph
    └── transactions/   # Transactions Ledger Screen & ViewModel
```

---

## ⚙️ The Equity Formula Mathematics

Given:
- Average Monthly Income: $I$
- Monthly Essential Expenses: $E$
- Discretionary Income: $D = \max(0, I - E)$
- Target Savings Goal: $G$
- Current Saved: $C$
- Remaining Amount: $R = \max(0, G - C)$
- Months Remaining: $M = \frac{\text{Days}}{30.4375}$

The calculations proceed as:
1. **Required Monthly Savings**:
   $$S_{month} = \frac{R}{M}$$
2. **Must Save Ratio**:
   $$R_{save} = \left(\frac{S_{month}}{I}\right) \times 100\%$$
3. **Safe to Spend (Monthly)**:
   $$S_{safe} = \max(0, D - S_{month})$$
4. **Daily Safe to Spend**:
   $$\text{Daily Allowance} = \frac{S_{safe}}{30.4375}$$
5. **Deposit Auto-Lock**:
   For any incoming deposit $X$:
   $$\text{Locked Amount} = X \times \left(\frac{R_{save}}{100}\right)$$

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- Android SDK 35 (compileSdk & targetSdk 35, minSdk 26)
- Java 21 LTS

### Gradle Commands
```powershell
# Run Unit Tests (including Equity Formula math verifications)
./gradlew test

# Assemble Debug APK
./gradlew assembleDebug

# Install and Launch on connected Android device/emulator
./gradlew installDebug
```