import { ReactNode } from "react";

export function PageContainer({ children }: { children: React.ReactNode }) {
	return (
		<div className="bg-slate-50">
			<div className="max-w-7xl mx-auto px-6 py-8">{children}</div>
		</div>
	);
}
