import { useDispatch, useSelector } from 'react-redux';
import type { TypedUseSelectorHook } from 'react-redux';
import type { RootState, AppDispatch } from '../../store';

/** Typed dispatch — use this instead of plain `useDispatch` */
export const useAppDispatch = () => useDispatch<AppDispatch>();

/** Typed selector — use this instead of plain `useSelector` */
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;
