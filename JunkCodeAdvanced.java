package com.android.system.qspaas;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerador avançado de junk code para ofuscação de DEX.
 * Focado em variabilidade estrutural sem overhead de runtime.
 */
public class JunkCodeAdvanced {

    private static final Random RANDOM = new Random();

    // Campos voláteis para impedir otimização de compilador/JIT
    private volatile int vInt;
    private volatile long vLong;
    private volatile double vDouble;
    private volatile String vString;
    private final List<String> junkList;

    public JunkCodeAdvanced() {
        this.vInt = RANDOM.nextInt(100000);
        this.vLong = RANDOM.nextLong();
        this.vDouble = RANDOM.nextDouble() * 5000;
        this.vString = generateEntropy(20);
        this.junkList = new ArrayList<>();

        // Preenche lista com dados aleatórios
        for (int i = 0; i < 15; i++) {
            junkList.add(generateEntropy(12));
        }

        // Executa operações iniciais para variar o estado inicial
        mutateState();
    }

    private String generateEntropy(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append((char) (RANDOM.nextInt(94) + 33));
        }
        return sb.toString();
    }

    public void run() {
        // Combinação de operações matemáticas e lógicas
        double temp = Math.sin(vInt) * Math.cos(vDouble) + Math.tan(vInt % 10);

        if (vLong > 0) {
            vString = vString + temp;
        } else {
            vString = new StringBuilder(vString).reverse().toString();
        }

        // Loop com complexidade ciclomática
        for (int i = 0; i < (vInt % 15) + 5; i++) {
            processBranch(i % 4);
        }

        // Processamento de lista para gerar bytecode de iterador
        for (String s : junkList) {
            vInt += s.hashCode();
        }

        // Chamadas de métodos privados para expandir o grafo de chamada
        dummyMath();
        dummyStringOps();
        dummyArrayOps();
    }

    private void processBranch(int key) {
        switch (key) {
            case 0:
                vInt = (vInt * 31) ^ 0xFF;
                break;
            case 1:
                vLong = vLong ^ (vLong << 5);
                break;
            case 2:
                vDouble = Math.sqrt(Math.abs(vDouble));
                break;
            default:
                vString = vString.substring(0, Math.min(5, vString.length()));
                break;
        }
    }

    private void mutateState() {
        // Try-catch vazio gera tabela de exceções no DEX
        try {
            if (vInt < 0) throw new IllegalArgumentException();
            vInt = vInt + 1;
        } catch (IllegalArgumentException e) {
            vInt = Math.abs(vInt);
        }
    }

    private void dummyMath() {
        int x = vInt * 2;
        int y = x / 3;
        int z = y % 7;
        vInt = x + y + z;
    }

    private void dummyStringOps() {
        String s = vString.toUpperCase();
        s = s.replace("A", "4").replace("E", "3");
        vString = s.substring(0, Math.min(10, s.length()));
    }

    private void dummyArrayOps() {
        int[] arr = new int[50];
        for (int i = 0; i < 50; i++) {
            arr[i] = i * vInt;
        }
        vInt = arr[49];
    }

    // Métodos públicos "fake" para aumentar a tabela de métodos
    public void extra1() { dummyMath(); }
    public void extra2() { dummyStringOps(); }
    public void extra3() { dummyArrayOps(); }
    public void extra4() { mutateState(); }
    public void extra5() { vLong = System.nanoTime(); }

    // Simulação de criptografia leve (apenas XOR) para adicionar complexidade
    public void cryptoJunk() {
        byte[] data = new byte[128];
        RANDOM.nextBytes(data);
        for (int i = 0; i < data.length; i++) {
            data[i] ^= (byte) (vInt & 0xFF);
        }
        vInt = java.util.Arrays.hashCode(data);
    }

    // Execução principal de todas as rotinas de junk
    public void executeAll() {
        run();
        extra1();
        extra2();
        extra3();
        extra4();
        extra5();
        cryptoJunk();
    }
}
