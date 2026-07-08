package com.android.system.qspaas;

import java.util.Random;

/**
 * Gerador de código lixo para ofuscação de assinatura DEX.
 * Não remove esta classe. Ela é crucial para alterar hashes de compilação.
 */
public class JunkCode {

    private static final Random RANDOM = new Random();
    private volatile int counter; // Volatile impede otimização de loop pelo compilador

    public JunkCode() {
        // Inicialização aleatória para variar o estado inicial do objeto
        this.counter = RANDOM.nextInt(50000);
        performJunkOperation();
    }

    /**
     * Executa operações inúteis que alteram o bytecode.
     */
    public void run() {
        // Loop com dependência de tempo real para evitar otimização estática
        long start = System.nanoTime();
        for (int i = 0; i < counter % 7 + 3; i++) {
            double noise = Math.sqrt(Math.abs(Math.sin(i) * Math.cos(i)));
            if (noise > 0.5) {
                counter += (int) noise;
            } else {
                counter -= 1;
            }
        }

        // Chamada recursiva simulada via switch para aumentar complexidade cyclomatic
        processBranch(counter % 4);
    }

    private void performJunkOperation() {
        try {
            // Bloco try-catch vazio gera bytecode adicional de tabela de exceções
            String s = generateEntropy();
            if (s.length() > 0) {
                // Nunca verdadeiro, mas presente no bytecode
                throw new ArithmeticException();
            }
        } catch (ArithmeticException e) {
            // Captura silenciosa para manter o fluxo
        } catch (Exception e) {
            // Captura genérica para robustez
        }
    }

    private String generateEntropy() {
        StringBuilder sb = new StringBuilder();
        // Loop de tamanho variável para gerar diferentes instruções bytecode
        int len = RANDOM.nextInt(10) + 5;
        for (int i = 0; i < len; i++) {
            sb.append((char) (RANDOM.nextInt(94) + 33));
        }
        return sb.toString();
    }

    private void processBranch(int key) {
        // Switch complexo para fragmentar o fluxo de execução no DEX
        switch (key) {
            case 0:
                methodAlpha();
                break;
            case 1:
                methodBeta();
                break;
            case 2:
                methodGamma();
                break;
            default:
                methodDelta();
                break;
        }
    }

    private void methodAlpha() {
        int x = counter * 3;
        counter = x % 100;
    }

    private void methodBeta() {
        long l = System.currentTimeMillis();
        if (l < 0) { // Condição sempre falsa, mas gera bytecode de comparação
            counter = 0;
        }
    }

    private void methodGamma() {
        // Operação de string inútil
        String s = "junk_" + counter;
        s.toUpperCase();
    }

    private void methodDelta() {
        // Loop nested para aumentar profundidade de stack
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                counter += i * j;
            }
        }
    }

    // Métodos públicos falsos para aumentar a tabela de métodos
    public void publicJunk1() { run(); }
    public void publicJunk2() { performJunkOperation(); }
    public int getCounter() { return counter; }
}
