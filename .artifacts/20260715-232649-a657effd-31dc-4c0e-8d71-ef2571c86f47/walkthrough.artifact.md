# Walkthrough - Suporte a PDFs com Senha

Implementamos a infraestrutura necessária para lidar com documentos PDF protegidos por senha, garantindo uma experiência de usuário fluida mesmo em arquivos seguros.

## O que foi feito

### 1. Sistema de Exceções de Segurança
Criamos uma hierarquia de exceções no `commonMain` para sinalizar estados de segurança:
- `PdfPasswordRequiredException`: Quando um PDF protegido é aberto sem senha.
- `PdfInvalidPasswordException`: Quando a senha fornecida não confere.

### 2. Contratos Atualizados
As interfaces `PdfRepository` e `PdfReader` agora aceitam um parâmetro opcional `password`, permitindo que credenciais sejam passadas durante o carregamento.

### 3. Detecção Automática (Android)
O motor `AndroidPdfRenderer` foi atualizado para capturar falhas de segurança e lançar as exceções KMP correspondentes, permitindo que a UI reaja à necessidade de senha.

### 4. Interface de Desbloqueio
O `PdfViewer` agora possui:
- **Diálogo Padrão**: Um diálogo Material 3 que solicita a senha automaticamente quando necessário.
- **Customização**: O desenvolvedor pode fornecer sua própria UI de senha através do novo parâmetro `passwordDialog`.
- **Fluxo de Re-tentativa**: O `PdfViewerState` gerencia o estado de bloqueio e permite tentar o desbloqueio sem recarregar todo o componente.

## Verificação Técnica

### Testes Unitários
Adicionamos 3 novos casos de teste ao `PdfViewerStateTest`:
- Detecção de documento protegido.
- Tratamento de senha inválida.
- Sucesso no carregamento após desbloqueio.

**Resultado**: **14 testes passados** (100% sucesso).

---
Trabalho realizado no branch **`feat/pdf-password-support`**.
