import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  transfersService,
  TransferApiError,
  type StorageMode,
  type Transfer,
  type TransferCreateRequest,
} from '../services/transfers'

export interface TransferStoreError {
  message: string
  code?: string
  status: number
  details: Record<string, string>
  isConflict: boolean
}

function toStoreError(error: unknown): TransferStoreError {
  if (error instanceof TransferApiError) {
    return {
      message: error.message,
      code: error.code,
      status: error.status,
      details: error.details,
      isConflict: error.isConflict,
    }
  }
  return {
    message: error instanceof Error ? error.message : 'Ocorreu um erro inesperado.',
    status: 0,
    details: {},
    isConflict: false,
  }
}

export const useTransfersStore = defineStore('transfers', () => {
  const transfers = ref<Transfer[]>([])
  const storageMode = ref<StorageMode | null>(null)
  const loading = ref(false)
  const submitting = ref(false)
  const listUnavailable = ref(false)
  const error = ref<TransferStoreError | null>(null)

  async function load() {
    loading.value = true
    error.value = null
    try {
      const result = await transfersService.list()
      transfers.value = result.transfers
      storageMode.value = result.storageMode
      listUnavailable.value = false
    } catch (cause) {
      listUnavailable.value = true
      error.value = toStoreError(cause)
    } finally {
      loading.value = false
    }
  }

  async function runMutation(operation: () => Promise<Transfer>) {
    submitting.value = true
    error.value = null
    try {
      await operation()
      await load()
      return true
    } catch (cause) {
      const mutationError = toStoreError(cause)
      if (mutationError.isConflict) {
        error.value = mutationError
        await load()
        error.value = mutationError
      } else {
        error.value = mutationError
      }
      return false
    } finally {
      submitting.value = false
    }
  }

  function create(input: TransferCreateRequest) {
    return runMutation(() => transfersService.create(input))
  }

  function approve(id: string) {
    return runMutation(() => transfersService.approve(id))
  }

  function reject(id: string) {
    return runMutation(() => transfersService.reject(id))
  }

  return { transfers, storageMode, loading, submitting, listUnavailable, error, load, create, approve, reject }
})
