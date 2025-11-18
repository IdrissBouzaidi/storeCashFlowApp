export const PRODUCT_TRANSACTION_STATES = {
    AVAILABLE: 1,
    RESERVED: 2,
    SOLD: 3,
    CANCELED_BEFORE_SALE: 4,
    CANCELED_AFTER_SALE: 5,
    RETURNED: 6,
    LOST: 7
} as const;

export const FINANCIAL_PERIOD_STATES = {
    PLAN: 1,
    IN_PROG: 2,
    CLOSED: 3,
    CANCELED: 4
} as const;

export const PRODUCT_STATES = {
    ACTIVE: 1,
    INACTIVE: 2,
    CANCELED: 3
} as const;