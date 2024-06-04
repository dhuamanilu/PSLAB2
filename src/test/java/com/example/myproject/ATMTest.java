
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.example.myproject.ATM;
import com.example.myproject.BankDatabase;
import com.example.myproject.CashDispenser;
import com.example.myproject.DepositSlot;
import com.example.myproject.Keypad;
import com.example.myproject.Screen;
import static org.junit.jupiter.api.Assertions.*;

class ATMTest {
    private ATM atm;
    private Screen screenMock;
    private Keypad keypadMock;
    private BankDatabase bankDatabaseMock;
    private CashDispenser cashDispenserMock;
    private DepositSlot depositSlotMock;

    @BeforeEach
    void setUp() {
        screenMock = mock(Screen.class);
        keypadMock = mock(Keypad.class);
        bankDatabaseMock = mock(BankDatabase.class);
        cashDispenserMock = mock(CashDispenser.class);
        depositSlotMock = mock(DepositSlot.class);
        
        atm = new ATM();
        
        atm.screen = screenMock;
        atm.keypad = keypadMock;
        atm.bankDatabase = bankDatabaseMock;
        atm.cashDispenser = cashDispenserMock;
        atm.depositSlot = depositSlotMock;
    }
    
    // TEST-0001: Prueba de autenticación de usuarios con credenciales correctas
    @Test
    void testAuthenticationWithCorrectCredentials() {
        
        when(keypadMock.getInput()).thenReturn(12345, 54321);
        when(bankDatabaseMock.authenticateUser(12345, 54321)).thenReturn(true);

        atm.run();
    
        InOrder inOrder = inOrder(screenMock);
        inOrder.verify(screenMock).displayMessageLine("\nWelcome!");
        inOrder.verify(screenMock).displayMessage("\nPlease enter your bank account number: ");
        inOrder.verify(screenMock).displayMessage("\nPlease enter your PIN: ");
        
        
    }

    // TEST-0002: Prueba de autenticación de usuarios con caracteres incorrectos en el número de cuenta
    @Test
    void testAuthenticationWithInvalidAccountNumber() {
        when(keypadMock.getInput()).thenThrow(NumberFormatException.class);

        atm.authenticateUser();

        verify(screenMock).displayMessage("\nPlease enter your bank account number: ");
        verify(screenMock).displayMessage("\nPlease enter your PIN: ");
        verify(screenMock).displayMessage("\nInvalid account number or PIN code. Please try again.");
    }

    // TEST-0003: Prueba de autenticación de usuarios con caracteres incorrectos en el PIN
    @Test
    void testAuthenticationWithInvalidPIN() {
        when(keypadMock.getInput()).thenReturn(12345);
        when(keypadMock.getInput()).thenThrow(NumberFormatException.class);

        atm.authenticateUser();

        verify(screenMock).displayMessage("\nPlease enter your bank account number: ");
        verify(screenMock).displayMessage("\nPlease enter your PIN: ");
        verify(screenMock).displayMessage("\nInvalid account number or PIN code. Please try again.");
    }

    // TEST-0004: Prueba de autenticación de usuarios con credenciales incorrectas
    @Test
    void testAuthenticationWithIncorrectCredentials() {
        when(keypadMock.getInput()).thenReturn(12345, 66789);
        when(bankDatabaseMock.authenticateUser(12345, 66789)).thenReturn(false);

        atm.authenticateUser();

        verify(screenMock).displayMessage("\nInvalid account number or PIN code. Please try again.");
    }

    // TEST-0005: Prueba de autenticación de usuarios con credenciales vacías
    @Test
    void testAuthenticationWithEmptyCredentials() {
        when(keypadMock.getInput()).thenReturn(-1); // Assuming -1 simulates Ctrl+Z

        atm.authenticateUser();

        verify(screenMock).displayMessage("\nPlease enter your bank account number: ");
        verify(screenMock).displayMessage("\nPlease enter your PIN: ");
        verify(screenMock).displayMessage("\nInvalid account number or PIN code. Please try again.");
    }

    // TEST-0006: Prueba de visualización de saldo
    @Test
    void testBalanceInquiry() {
        when(keypadMock.getInput()).thenReturn(12345, 54321, 1, 4);
        when(bankDatabaseMock.authenticateUser(12345, 54321)).thenReturn(true);
        when(bankDatabaseMock.getAvailableBalance(12345)).thenReturn(1000.0);
        when(bankDatabaseMock.getTotalBalance(12345)).thenReturn(1200.0);

        atm.run();

        InOrder inOrder = inOrder(screenMock);
        inOrder.verify(screenMock).displayMessageLine("\nWelcome!");
        inOrder.verify(screenMock).displayMessage("\nPlease enter your bank account number: ");
        inOrder.verify(screenMock).displayMessage("\nPlease enter your PIN: ");
        inOrder.verify(screenMock).displayMessageLine("\nBalance Information:");
        inOrder.verify(screenMock).displayMessage(" - Available balance: ");
        inOrder.verify(screenMock).dispalyDollarAmount(1000.0);
        inOrder.verify(screenMock).displayMessage("\n - Total balance:");
        inOrder.verify(screenMock).dispalyDollarAmount(1200.0);
        inOrder.verify(screenMock).displayMessageLine("");
    }

    // TEST-0007: Prueba de visualización de saldo después de un retiro
    @Test
    void testBalanceAfterWithdrawal() {
        when(keypadMock.getInput()).thenReturn(12345, 54321, 2, 1, 1, 4);
        when(bankDatabaseMock.authenticateUser(12345, 54321)).thenReturn(true);
        when(bankDatabaseMock.getAvailableBalance(12345)).thenReturn(1000.0, 980.0);
        when(bankDatabaseMock.getTotalBalance(12345)).thenReturn(1200.0, 1180.0);
        when(cashDispenserMock.isSufficientCashAvailable(20)).thenReturn(true);

        atm.run();

        InOrder inOrder = inOrder(screenMock);
        inOrder.verify(screenMock).displayMessageLine("\nWelcome!");
        inOrder.verify(screenMock).displayMessage("\nPlease enter your bank account number: ");
        inOrder.verify(screenMock).displayMessage("\nPlease enter your PIN: ");
        inOrder.verify(screenMock).displayMessageLine("\nYour cash has been dispensed. Please take your cash now.");
        inOrder.verify(screenMock, times(2)).displayMessageLine("\nBalance Information:");
        inOrder.verify(screenMock, times(2)).displayMessage(" - Available balance: ");
        inOrder.verify(screenMock).dispalyDollarAmount(980.0);
        inOrder.verify(screenMock, times(2)).displayMessage("\n - Total balance:");
        inOrder.verify(screenMock).dispalyDollarAmount(1180.0);
        inOrder.verify(screenMock).displayMessageLine("");
    }

    // TEST-0008: Prueba de visualización de saldo después de un depósito
    @Test
    void testBalanceAfterDeposit() {
        when(keypadMock.getInput()).thenReturn(12345, 54321, 3, 5000, 1, 4);
        when(bankDatabaseMock.authenticateUser(12345, 54321)).thenReturn(true);
        when(bankDatabaseMock.getAvailableBalance(12345)).thenReturn(1000.0, 1050.0);
        when(bankDatabaseMock.getTotalBalance(12345)).thenReturn(1200.0, 1250.0);
        when(depositSlotMock.isEnvelopeReceived()).thenReturn(true);

        atm.run();

        InOrder inOrder = inOrder(screenMock);
        inOrder.verify(screenMock).displayMessageLine("\nWelcome!");
        inOrder.verify(screenMock).displayMessage("\nPlease enter your bank account number: ");
        inOrder.verify(screenMock).displayMessage("\nPlease enter your PIN: ");
        inOrder.verify(screenMock).displayMessage("\nPlease insert a deposit envelope containing ");
        inOrder.verify(screenMock).dispalyDollarAmount(50.0);
        inOrder.verify(screenMock).displayMessageLine(".");
        inOrder.verify(screenMock).displayMessage("\nYour envelope has been received.\nNOTE: The money just deposited will not be available until we verify the amount of any enclosed cash and your checks clear.");
        inOrder.verify(screenMock, times(2)).displayMessageLine("\nBalance Information:");
        inOrder.verify(screenMock, times(2)).displayMessage(" - Available balance: ");
        inOrder.verify(screenMock).dispalyDollarAmount(1050.0);
        inOrder.verify(screenMock, times(2)).displayMessage("\n - Total balance:");
        inOrder.verify(screenMock).dispalyDollarAmount(1250.0);
        inOrder.verify(screenMock).displayMessageLine("");
    }

    
}

