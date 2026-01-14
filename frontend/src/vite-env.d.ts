/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly BACKEND_ENDPOINT?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

declare module '*.svg' {
  const content: string;
  export default content;
}

declare module '*.avif' {
  const content: string;
  export default content;
}
