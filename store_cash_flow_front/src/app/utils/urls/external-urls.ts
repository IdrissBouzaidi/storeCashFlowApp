const BASE_URL = '/api/v1';

// Login
export const LOGIN_URL: string = BASE_URL + '/login';

// User infos(name, roles, ...)
const USER_URL: string = BASE_URL + '/user';
export const GET_USER_INFORMATIONS_URL: string = USER_URL;
export const GET_USER_ACTIONS_URL: string = USER_URL + '/actions';

//Transactions
export const TRANSACTIIONS_URL: string = BASE_URL + '/transactions';

// Consumable input
export const CONS_INPUTS_URL: string = BASE_URL + '/consInputs';
export const SEARCH_CONS_INPUTS_URL: string = CONS_INPUTS_URL + '/search';
export const ADD_CONS_INPUT_LIST_URL: string = CONS_INPUTS_URL + '/list';
export const CANCEL_CONS_INPUT_URL: string = CONS_INPUTS_URL + '/cancel';

// Not consumable inputs
export const NOT_CONS_INPUT_URL: string = BASE_URL + '/notConsInputs';
export const CANCEL_NOT_CONS_INPUT_URL: string = NOT_CONS_INPUT_URL + '/cancel';

// Outputs
export const OUTPUTS_URL: string = BASE_URL + '/outputs';
export const CANCEL_OUTPUT_URL: string = OUTPUTS_URL + '/cancel';

// Charges
export const CHARGES_URL: string = BASE_URL + '/charges';
export const CANCEL_CHARGE_URL: string = CHARGES_URL + '/cancel';

// Capital contributions
export const CAPITAL_CONTRIBUTIONS_URL: string = BASE_URL + '/capitalContributions';
export const CANCEL_CAPITAL_CONTRIBUTION_URL: string = CAPITAL_CONTRIBUTIONS_URL + '/cancel';

// Advances
export const ADVANCES_URL: string = BASE_URL + '/advances';
export const CANCEL_ADVANCE_URL: string = ADVANCES_URL + '/cancel';

// Customer credits
export const CUSTOMER_CREDITS_URL: string = BASE_URL + '/customerCredits';
export const CANCEL_CUSTOMER_CREDIT_URL: string = CUSTOMER_CREDITS_URL + '/cancel';

// External loans
export const EXTERNAL_LOANS_URL: string = BASE_URL + '/externalLoans';
export const CANCEL_EXTERNAL_LOAN_URL: string = EXTERNAL_LOANS_URL + '/cancel';

// Out of pockets
export const OUT_OF_POCKET_URL: string = BASE_URL + '/outOfPockets';
export const CANCEL_OUT_OF_POCKET_URL: string = OUT_OF_POCKET_URL + '/cancel';


//Financial periods
const FINANCIAL_PERIODS_URL: string = BASE_URL + '/financialPeriods';
export const GET_FINANCIAL_PERIODS_URL: string = FINANCIAL_PERIODS_URL;
export const IS_SOME_PERIOD_IN_PROGRESS_URL: string = FINANCIAL_PERIODS_URL + '/isSomePeriodInProgress';
export const GET_LAST_PERIOD_URL: string = FINANCIAL_PERIODS_URL + '/last';
export const CLOSE_CURRENT_PERIOD_URL: string = FINANCIAL_PERIODS_URL + '/closeCurrent';
export const CANCEL_PERIOD_URL: string = FINANCIAL_PERIODS_URL + '/cancel';
export const REOPEN_PERIOD_URL: string = FINANCIAL_PERIODS_URL + '/reopen';

//Products
const PRODUCTS_URL: string = BASE_URL + '/products';
export const GET_PRODUCTS_URL: string = PRODUCTS_URL;
export const CANCEL_PRODUCT_URL: string = PRODUCTS_URL + '/cancel';

//RefTables
const REF_TABLES_URL: string = BASE_URL + '/refTables'
export const GET_TRANSACTION_TYPES_REF_TABLE_URL: string = REF_TABLES_URL + '/transactionTypes';
export const GET_PERIODS_REF_TABLE_URL: string = REF_TABLES_URL + '/financialPeriods';
export const GET_PERIODS_STATES_REF_TABLE_URL: string = REF_TABLES_URL + '/periodStates';
export const GET_USERS_REF_TABLE_URL: string = REF_TABLES_URL + '/users';
export const GET_PRODUCTS_TRANSACTION_STATES_REF_TABLE_URL: string = REF_TABLES_URL + '/productsTransactionStates';
export const GET_CHARGES_TRANSACTION_STATES_REF_TABLE_URL: string = REF_TABLES_URL + '/chargesTransactionStates';
export const GET_CAPITAL_CONTRIBUTION_TRANSACTION_STATES_REF_TABLE_URL: string = REF_TABLES_URL + '/capitalContributionTransactionStates';
export const GET_ADVANCE_STATES_REF_TABLE_URL: string = REF_TABLES_URL + '/advanceStates';
export const GET_OUT_OF_POCKET_STATES_REF_TABLE_URL: string = REF_TABLES_URL + '/outOfPocketStates';
export const GET_PRODUCTS_REF_TABLE_URL: string = REF_TABLES_URL + '/products';
export const GET_CATEGORIES_REF_TABLE_URL: string = REF_TABLES_URL + '/categories';
export const GET_CHARGE_TYPES_URL: string = REF_TABLES_URL + '/chargeTypes';
export const GET_REUSABLE_INPUTS_REF_TABLE_URL: string = REF_TABLES_URL + '/reusableInputs';
export const GET_NOT_CONS_INPUT_STATES_REF_TABLE_URL: string = REF_TABLES_URL + '/notConsInputStates';
export const GET_CUSTOMER_CREDITS_STATES_REF_TABLE_URL: string = REF_TABLES_URL + '/customerCreditStates';
export const GET_EXTERNAL_LOAN_STATES_REF_TABLE_URL: string = REF_TABLES_URL + '/externalLoanStates';
export const GET_CUSTOMERS_REF_TABLE_URL: string = REF_TABLES_URL + '/customers';
export const GET_PRODUCT_STATES_REF_TABLE_URL: string = REF_TABLES_URL + '/productStates';
export const GET_CATEGORY_STATES_REF_TABLE_URL: string = REF_TABLES_URL + '/categoryStates';

// MinIO API
export const MIN_IO_API_URL: string = BASE_URL + '/minIoApi';
export const MIN_IO_FILES_URLS_URL: string = MIN_IO_API_URL + '/filesUrls';

//MinIO
export const MIN_IO_ABSOLUTE_URL: string = 'http://minio:9000';
export const MIN_IO_URL: string = BASE_URL + '/minIO';